# Cloud Functions - Yoga Video 16:9 Transcoding

## Context

This document describes the Cloud Functions modifications needed in the **OraWebApp** project to support 16:9 landscape video format for yoga content.

**Project**: `C:\Users\chris\source\repos\OraWebApp`
**Related Android PR**: [PR #69](https://github.com/Chrisdesmurger/Ora/pull/69)

---

## Overview

The Android app now displays yoga videos in **16:9 landscape format**. The Cloud Functions in OraWebApp need to be updated to:

1. **Handle aspect ratio conversion** for source videos that aren't 16:9
2. **Add content-type specific transcoding profiles** (yoga vs meditation)

---

## Implementation Tasks

### Issue #5: Aspect Ratio Handling

**File**: `functions/src/utils/ffmpeg-wrapper.ts`

Add aspect ratio calculation and filter generation:

```typescript
export interface AspectRatioConfig {
  targetRatio: number;  // 16/9 = 1.777
  mode: 'crop' | 'letterbox' | 'auto';
}

export const DEFAULT_ASPECT_CONFIG: AspectRatioConfig = {
  targetRatio: 16 / 9,
  mode: 'auto',  // User selected: Auto mode
};

/**
 * Calculate FFmpeg filter for aspect ratio conversion.
 *
 * Mode 'auto' (selected):
 * - Crop if source is wider than 16:9
 * - Letterbox if source is taller than 16:9
 */
export function calculateAspectFilter(
  sourceWidth: number,
  sourceHeight: number,
  targetWidth: number,
  targetHeight: number,
  mode: 'crop' | 'letterbox' | 'auto'
): string {
  const sourceRatio = sourceWidth / sourceHeight;
  const targetRatio = targetWidth / targetHeight;

  const effectiveMode = mode === 'auto'
    ? (sourceRatio > targetRatio ? 'crop' : 'letterbox')
    : mode;

  if (effectiveMode === 'crop') {
    // Center crop to target aspect ratio
    const cropWidth = Math.floor(sourceHeight * targetRatio);
    const cropX = Math.floor((sourceWidth - cropWidth) / 2);
    return `crop=${cropWidth}:${sourceHeight}:${cropX}:0,scale=${targetWidth}:${targetHeight}`;
  } else {
    // Letterbox (black bars) to target aspect ratio
    const scaledWidth = Math.floor(sourceWidth * (targetHeight / sourceHeight));
    const padX = Math.floor((targetWidth - scaledWidth) / 2);
    return `scale=${scaledWidth}:${targetHeight},pad=${targetWidth}:${targetHeight}:${padX}:0:black`;
  }
}
```

### Issue #6: Content-Type Specific Profiles

**File**: `functions/src/utils/ffmpeg-wrapper.ts`

Add yoga-specific transcoding profile with higher bitrate:

```typescript
// Higher bitrate for yoga to preserve instructor detail
export const YOGA_VIDEO_RENDITIONS: VideoRenditionConfig[] = [
  { quality: 'high', width: 1920, height: 1080, bitrate: '6000k' },
  { quality: 'medium', width: 1280, height: 720, bitrate: '3000k' },
  { quality: 'low', width: 854, height: 480, bitrate: '1200k' },
];

// Standard bitrate for meditation (less movement)
export const MEDITATION_VIDEO_RENDITIONS: VideoRenditionConfig[] = [
  { quality: 'high', width: 1920, height: 1080, bitrate: '4000k' },
  { quality: 'medium', width: 1280, height: 720, bitrate: '2000k' },
  { quality: 'low', width: 640, height: 360, bitrate: '800k' },
];

export function getRenditionsForContentType(contentType: string): VideoRenditionConfig[] {
  switch (contentType.toLowerCase()) {
    case 'yoga':
    case 'pilates':
      return YOGA_VIDEO_RENDITIONS;
    case 'meditation':
    case 'breathing':
      return MEDITATION_VIDEO_RENDITIONS;
    default:
      return VIDEO_RENDITIONS;  // Default profile
  }
}
```

### Update transcodeOnFinalize.ts

**File**: `functions/src/transcodeOnFinalize.ts`

Integrate aspect ratio handling into the transcoding flow:

```typescript
import { calculateAspectFilter, getRenditionsForContentType, DEFAULT_ASPECT_CONFIG } from './utils/ffmpeg-wrapper';

// In the transcoding function:
export async function transcodeVideo(
  inputPath: string,
  outputPath: string,
  config: VideoRenditionConfig,
  sourceMetadata?: MediaMetadata,
  aspectConfig: AspectRatioConfig = DEFAULT_ASPECT_CONFIG
): Promise<void> {
  return new Promise((resolve, reject) => {
    let command = ffmpeg(inputPath)
      .output(outputPath)
      .videoCodec('libx264')
      .audioCodec('aac')
      .audioBitrate('128k')
      .videoBitrate(config.bitrate);

    // Apply aspect ratio filter if source dimensions known
    if (sourceMetadata?.width && sourceMetadata?.height) {
      const filter = calculateAspectFilter(
        sourceMetadata.width,
        sourceMetadata.height,
        config.width,
        config.height,
        aspectConfig.mode
      );
      command = command.videoFilters(filter);
    } else {
      // Fallback to simple scaling (may distort)
      command = command.size(`${config.width}x${config.height}`);
    }

    command
      .outputOptions([
        '-preset fast',
        '-crf 23',
        '-movflags +faststart',
      ])
      .on('end', () => resolve())
      .on('error', (err) => reject(err))
      .run();
  });
}
```

### Update Firestore Document Metadata

Add aspect ratio metadata to lesson documents:

```typescript
// After transcoding, update the lesson document:
const updateData = {
  // ... existing fields
  source_aspect_ratio: `${metadata.width}:${metadata.height}`,
  output_aspect_ratio: '16:9',
  aspect_conversion_mode: effectiveMode,  // 'crop' | 'letterbox' | 'none'
};

await firestore.collection('lessons').doc(lessonId).update(updateData);
```

---

## Testing

### Unit Tests

```typescript
describe('calculateAspectFilter', () => {
  it('should crop 21:9 ultrawide to 16:9', () => {
    const filter = calculateAspectFilter(2560, 1080, 1920, 1080, 'auto');
    expect(filter).toContain('crop=');
  });

  it('should letterbox 4:3 to 16:9', () => {
    const filter = calculateAspectFilter(1440, 1080, 1920, 1080, 'auto');
    expect(filter).toContain('pad=');
  });

  it('should pass through 16:9 without modification', () => {
    const filter = calculateAspectFilter(1920, 1080, 1920, 1080, 'auto');
    expect(filter).toContain('scale=1920:1080');
  });
});

describe('getRenditionsForContentType', () => {
  it('should return yoga profile for yoga content', () => {
    const renditions = getRenditionsForContentType('yoga');
    expect(renditions[0].bitrate).toBe('6000k');
  });

  it('should return default profile for unknown content', () => {
    const renditions = getRenditionsForContentType('unknown');
    expect(renditions).toBe(VIDEO_RENDITIONS);
  });
});
```

### Integration Tests

1. Upload a 4:3 video → Verify 16:9 output with letterboxing
2. Upload a 21:9 video → Verify 16:9 output with cropping
3. Upload a 16:9 video → Verify no distortion
4. Upload a portrait video → Verify proper handling

---

## GitHub Issues to Create in OraWebApp

### Issue 1: Aspect Ratio Handling

**Title**: `feat(functions): Handle aspect ratio conversion for 16:9 output`

**Body**:
```
Add aspect ratio handling to video transcoding Cloud Function.

- Detect source video aspect ratio via ffprobe
- Apply center-crop for videos wider than 16:9
- Apply letterboxing (black bars) for videos taller than 16:9
- Mode: Auto (adaptatif) - selected by user

See: Ora PR #69
```

### Issue 2: Content-Type Profiles

**Title**: `feat(functions): Add content-type specific transcoding profiles`

**Body**:
```
Create different transcoding profiles for content types:

- Yoga/Pilates: Higher bitrate (6000k high) for instructor detail
- Meditation/Breathing: Standard bitrate (4000k high)
- Default: Existing profile

See: Ora PR #69
```

---

## Deployment

```bash
cd C:\Users\chris\source\repos\OraWebApp
npm run test  # Run unit tests
firebase deploy --only functions
```

---

## Related Files

| File | Description |
|------|-------------|
| `functions/src/utils/ffmpeg-wrapper.ts` | FFmpeg utility with aspect ratio and profiles |
| `functions/src/transcodeOnFinalize.ts` | Main transcoding Cloud Function |
| `functions/src/utils/media-metadata.ts` | Media metadata extraction |

---

**Created**: 2026-02-05
**Android PR**: [#69](https://github.com/Chrisdesmurger/Ora/pull/69)
**Status**: Ready for implementation in OraWebApp
