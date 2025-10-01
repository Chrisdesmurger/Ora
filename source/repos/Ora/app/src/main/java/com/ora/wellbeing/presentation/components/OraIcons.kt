package com.ora.wellbeing.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Icônes personnalisées pour l'application Ora.
 * Utilise Material Icons où possible, avec des icônes custom pour les pratiques spécifiques.
 */
object OraIcons {

    /**
     * Icône Yoga - personne en position de méditation/lotus
     */
    val Yoga: ImageVector
        get() = Icons.Default.SelfImprovement

    /**
     * Icône Pilates - personne en mouvement/exercice
     */
    val Pilates: ImageVector
        get() = Icons.Default.FitnessCenter

    /**
     * Icône Méditation - lotus ou position zen
     */
    val Meditation: ImageVector
        get() = Icons.Default.Spa

    /**
     * Icône Respiration - cercle avec vagues ou @ stylisé
     */
    val Respiration: ImageVector
        get() = Icons.Default.Air

    /**
     * Icône Gratitude - coeur ou étoile
     */
    val Gratitude: ImageVector
        get() = Icons.Default.FavoriteBorder

    /**
     * Icône Objectifs - cible ou check
     */
    val Goals: ImageVector
        get() = Icons.Default.CheckCircleOutline

    /**
     * Icône Streak (jours d'affilée) - flamme
     */
    val Streak: ImageVector
        get() = Icons.Default.LocalFireDepartment

    /**
     * Icône Temps total - horloge
     */
    val TotalTime: ImageVector
        get() = Icons.Default.Schedule

    /**
     * Icône Dernière activité - play ou historique
     */
    val LastActivity: ImageVector
        get() = Icons.Default.History

    /**
     * Icône Calendrier
     */
    val Calendar: ImageVector
        get() = Icons.Default.CalendarToday

    /**
     * Icône Badge/Récompense
     */
    val Badge: ImageVector
        get() = Icons.Default.EmojiEvents

    /**
     * Icône Paramètres
     */
    val Settings: ImageVector
        get() = Icons.Default.Settings

    /**
     * Icône Modifier/Editer
     */
    val Edit: ImageVector
        get() = Icons.Default.Edit

    /**
     * Icône Fermer
     */
    val Close: ImageVector
        get() = Icons.Default.Close

    /**
     * Icône Play
     */
    val Play: ImageVector
        get() = Icons.Default.PlayArrow

    /**
     * Icône Pause
     */
    val Pause: ImageVector
        get() = Icons.Default.Pause

    /**
     * Icône Favoris
     */
    val Favorite: ImageVector
        get() = Icons.Default.Favorite

    /**
     * Icône Recherche
     */
    val Search: ImageVector
        get() = Icons.Default.Search

    /**
     * Icône Filtre
     */
    val Filter: ImageVector
        get() = Icons.Default.FilterList

    /**
     * Icône Accueil
     */
    val Home: ImageVector
        get() = Icons.Default.Home

    /**
     * Icône Bibliothèque
     */
    val Library: ImageVector
        get() = Icons.Default.VideoLibrary

    /**
     * Icône Journal
     */
    val Journal: ImageVector
        get() = Icons.Default.MenuBook

    /**
     * Icône Programmes
     */
    val Programs: ImageVector
        get() = Icons.Default.Layers

    /**
     * Icône Profil
     */
    val Profile: ImageVector
        get() = Icons.Default.Person

    /**
     * Icône Notification
     */
    val Notification: ImageVector
        get() = Icons.Default.Notifications

    /**
     * Icône Notif OFF
     */
    val NotificationOff: ImageVector
        get() = Icons.Default.NotificationsOff

    /**
     * Icône Mode sombre
     */
    val DarkMode: ImageVector
        get() = Icons.Default.DarkMode

    /**
     * Icône Mode clair
     */
    val LightMode: ImageVector
        get() = Icons.Default.LightMode

    /**
     * Icône Info
     */
    val Info: ImageVector
        get() = Icons.Default.Info

    /**
     * Icône Partager
     */
    val Share: ImageVector
        get() = Icons.Default.Share

    /**
     * Icône Télécharger
     */
    val Download: ImageVector
        get() = Icons.Default.Download

    /**
     * Icône Plus/Ajouter
     */
    val Add: ImageVector
        get() = Icons.Default.Add

    /**
     * Icône Check/Validé
     */
    val Check: ImageVector
        get() = Icons.Default.Check

    /**
     * Icône Flèche retour
     */
    val ArrowBack: ImageVector
        get() = Icons.Default.ArrowBack

    /**
     * Icône Flèche avant
     */
    val ArrowForward: ImageVector
        get() = Icons.Default.ArrowForward
}