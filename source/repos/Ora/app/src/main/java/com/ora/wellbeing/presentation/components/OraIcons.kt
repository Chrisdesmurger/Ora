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
 * Icones personnalisees pour l'application Ora.
 * Utilise Material Icons ou possible, avec des icones custom pour les pratiques specifiques.
 */
object OraIcons {

    /**
     * Icone Yoga - personne en position de meditation/lotus
     */
    val Yoga: ImageVector
        get() = Icons.Default.SelfImprovement

    /**
     * Icone Pilates - personne en mouvement/exercice
     */
    val Pilates: ImageVector
        get() = Icons.Default.FitnessCenter

    /**
     * Icone Meditation - lotus ou position zen
     */
    val Meditation: ImageVector
        get() = Icons.Default.Spa

    /**
     * Icone Respiration - cercle avec vagues ou @ stylise
     */
    val Respiration: ImageVector
        get() = Icons.Default.Air

    /**
     * Icone Auto-massage - mains ou toucher therapeutique
     */
    val AutoMassage: ImageVector
        get() = Icons.Default.BackHand

    /**
     * Icone Gratitude - coeur ou etoile
     */
    val Gratitude: ImageVector
        get() = Icons.Default.FavoriteBorder

    /**
     * Icone Objectifs - cible ou check
     */
    val Goals: ImageVector
        get() = Icons.Default.CheckCircleOutline

    /**
     * Icone Streak (jours d'affilee) - flamme
     */
    val Streak: ImageVector
        get() = Icons.Default.LocalFireDepartment

    /**
     * Icone Temps total - horloge
     */
    val TotalTime: ImageVector
        get() = Icons.Default.Schedule

    /**
     * Icone Derniere activite - play ou historique
     */
    val LastActivity: ImageVector
        get() = Icons.Default.History

    /**
     * Icone Calendrier
     */
    val Calendar: ImageVector
        get() = Icons.Default.CalendarToday

    /**
     * Icone Badge/Recompense
     */
    val Badge: ImageVector
        get() = Icons.Default.EmojiEvents

    /**
     * Icone Parametres
     */
    val Settings: ImageVector
        get() = Icons.Default.Settings

    /**
     * Icone Modifier/Editer
     */
    val Edit: ImageVector
        get() = Icons.Default.Edit

    /**
     * Icone Fermer
     */
    val Close: ImageVector
        get() = Icons.Default.Close

    /**
     * Icone Play
     */
    val Play: ImageVector
        get() = Icons.Default.PlayArrow

    /**
     * Icone Pause
     */
    val Pause: ImageVector
        get() = Icons.Default.Pause

    /**
     * Icone Favoris
     */
    val Favorite: ImageVector
        get() = Icons.Default.Favorite

    /**
     * Icone Recherche
     */
    val Search: ImageVector
        get() = Icons.Default.Search

    /**
     * Icone Filtre
     */
    val Filter: ImageVector
        get() = Icons.Default.FilterList

    /**
     * Icone Accueil
     */
    val Home: ImageVector
        get() = Icons.Default.Home

    /**
     * Icone Bibliotheque
     */
    val Library: ImageVector
        get() = Icons.Default.VideoLibrary

    /**
     * Icone Journal
     */
    val Journal: ImageVector
        get() = Icons.Default.MenuBook

    /**
     * Icone Programmes
     */
    val Programs: ImageVector
        get() = Icons.Default.Layers

    /**
     * Icone Profil
     */
    val Profile: ImageVector
        get() = Icons.Default.Person

    /**
     * Icone Notification
     */
    val Notification: ImageVector
        get() = Icons.Default.Notifications

    /**
     * Icone Notif OFF
     */
    val NotificationOff: ImageVector
        get() = Icons.Default.NotificationsOff

    /**
     * Icone Mode sombre
     */
    val DarkMode: ImageVector
        get() = Icons.Default.DarkMode

    /**
     * Icone Mode clair
     */
    val LightMode: ImageVector
        get() = Icons.Default.LightMode

    /**
     * Icone Info
     */
    val Info: ImageVector
        get() = Icons.Default.Info

    /**
     * Icone Partager
     */
    val Share: ImageVector
        get() = Icons.Default.Share

    /**
     * Icone Telecharger
     */
    val Download: ImageVector
        get() = Icons.Default.Download

    /**
     * Icone Plus/Ajouter
     */
    val Add: ImageVector
        get() = Icons.Default.Add

    /**
     * Icone Check/Valide
     */
    val Check: ImageVector
        get() = Icons.Default.Check

    /**
     * Icone Fleche retour
     */
    val ArrowBack: ImageVector
        get() = Icons.Default.ArrowBack

    /**
     * Icone Fleche avant
     */
    val ArrowForward: ImageVector
        get() = Icons.Default.ArrowForward

    /**
     * Icone Vagues pour Respiration - 3 lignes ondulees
     */
    val Waves: ImageVector
        get() = Icons.Default.Air

    /**
     * Icone Personne en position Lotus/Yoga
     */
    val YogaPerson: ImageVector
        get() = Icons.Default.SelfImprovement

    /**
     * Icone Tete/Profil pour Meditation
     */
    val MindHead: ImageVector
        get() = Icons.Default.Psychology
}
