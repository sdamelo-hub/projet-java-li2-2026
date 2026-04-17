-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : ven. 17 avr. 2026 à 04:34
-- Version du serveur : 10.4.32-MariaDB
-- Version de PHP : 8.1.25

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `univ_scheduler_db`
--

-- --------------------------------------------------------

--
-- Structure de la table `administrateurs`
--

CREATE TABLE `administrateurs` (
  `idAdmin` varchar(255) DEFAULT NULL,
  `identifiantConnexion` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `batiments`
--

CREATE TABLE `batiments` (
  `nbEtage` int(11) NOT NULL,
  `code_batiment` varchar(255) NOT NULL,
  `localisationBatiment` varchar(255) DEFAULT NULL,
  `nomBatiment` varchar(255) DEFAULT NULL,
  `typeBatiment` varchar(255) DEFAULT NULL,
  `nom_batiment` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `batiments`
--

INSERT INTO `batiments` (`nbEtage`, `code_batiment`, `localisationBatiment`, `nomBatiment`, `typeBatiment`, `nom_batiment`) VALUES
(2, 'A', 'Nord du Campus', 'Faculte de Medecine', 'Enseignement', NULL),
(3, 'B', 'Sud du Campus', 'Faculte des Lettres', 'Enseignement', NULL),
(3, 'BAT-C', 'Partie Est', 'Faculté de mathématiques', 'Pédagogique', NULL),
(3, 'BAT-Z', 'Campus Nord', 'Faculté de Sociologie', 'Pédagogique', NULL),
(2, 'C', 'Campus Est', 'Faculté des droits', 'Pédagogique', NULL);

-- --------------------------------------------------------

--
-- Structure de la table `cours`
--

CREATE TABLE `cours` (
  `heuresEffectuees` double NOT NULL,
  `nbrHeure` double NOT NULL,
  `codeCours` varchar(255) NOT NULL,
  `intituleMatiere` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `cours`
--

INSERT INTO `cours` (`heuresEffectuees`, `nbrHeure`, `codeCours`, `intituleMatiere`) VALUES
(0, 40, 'INF230', 'Algorithme et Programmation');

-- --------------------------------------------------------

--
-- Structure de la table `creneaux`
--

CREATE TABLE `creneaux` (
  `dateSeance` date DEFAULT NULL,
  `duree` double DEFAULT NULL,
  `heureDebut` time(6) DEFAULT NULL,
  `heureFin` time(6) DEFAULT NULL,
  `code_cours` varchar(255) DEFAULT NULL,
  `idCreneau` varchar(255) NOT NULL,
  `numero_salle` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `creneaux`
--

INSERT INTO `creneaux` (`dateSeance`, `duree`, `heureDebut`, `heureFin`, `code_cours`, `idCreneau`, `numero_salle`) VALUES
('2026-04-17', 2, '09:00:00.000000', '11:00:00.000000', 'INF230', 'CR-BA21EE30', 'A1');

-- --------------------------------------------------------

--
-- Structure de la table `departements`
--

CREATE TABLE `departements` (
  `codeDepartement` varchar(255) NOT NULL,
  `code_ufr` varchar(255) DEFAULT NULL,
  `nomDepartement` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `departements`
--

INSERT INTO `departements` (`codeDepartement`, `code_ufr`, `nomDepartement`) VALUES
('INFO', 'SET', 'Informatique'),
('MED', '2S', 'Medecine');

-- --------------------------------------------------------

--
-- Structure de la table `enseignants`
--

CREATE TABLE `enseignants` (
  `chargeHoraireAnnuelle` double NOT NULL,
  `grade` varchar(255) DEFAULT NULL,
  `idEnseignant` varchar(255) DEFAULT NULL,
  `identifiantConnexion` varchar(255) NOT NULL,
  `specialite` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `equipements`
--

CREATE TABLE `equipements` (
  `etatFonctionnement` varchar(255) DEFAULT NULL,
  `idEquipement` varchar(255) NOT NULL,
  `nomEquipement` varchar(255) DEFAULT NULL,
  `numero_salle` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `equipements`
--

INSERT INTO `equipements` (`etatFonctionnement`, `idEquipement`, `nomEquipement`, `numero_salle`) VALUES
('Opérationnel', 'E43', 'Vidéoprojecteur', 'Z1'),
('Opérationnel', 'VP1', 'Tableau Interactif', 'A3'),
('En panne', 'VP2', 'Vidéoprojecteur', 'B50'),
('En panne', 'VP3', 'Climatisation', 'A5');

-- --------------------------------------------------------

--
-- Structure de la table `etudiants`
--

CREATE TABLE `etudiants` (
  `code_groupe` varchar(255) DEFAULT NULL,
  `cycle` varchar(255) DEFAULT NULL,
  `identifiantConnexion` varchar(255) NOT NULL,
  `ineEtudiant` varchar(255) DEFAULT NULL,
  `niveauEtude` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `filieres`
--

CREATE TABLE `filieres` (
  `codeFiliere` varchar(255) NOT NULL,
  `code_departement` varchar(255) DEFAULT NULL,
  `nomFiliere` varchar(255) DEFAULT NULL,
  `responsableFiliere` varchar(255) DEFAULT NULL,
  `niveauDiplome` enum('LICENCE','MASTER','DOCTORAT') DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `gestionnaires`
--

CREATE TABLE `gestionnaires` (
  `idGestionnaire` int(11) NOT NULL,
  `identifiantConnexion` varchar(255) NOT NULL,
  `responsabilite` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `groupes`
--

CREATE TABLE `groupes` (
  `effectifGroupe` int(11) NOT NULL,
  `numeroGroupe` int(11) NOT NULL,
  `codeGroupe` varchar(255) NOT NULL,
  `code_promotion` varchar(255) DEFAULT NULL,
  `modaliteGroupe` varchar(255) DEFAULT NULL,
  `specialiteGroupe` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `incidents`
--

CREATE TABLE `incidents` (
  `idIncident` int(11) NOT NULL,
  `dateSignalement` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `id_enseignant` varchar(255) DEFAULT NULL,
  `id_equipement` varchar(255) DEFAULT NULL,
  `numero_salle` varchar(255) DEFAULT NULL,
  `statut` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `notifications`
--

CREATE TABLE `notifications` (
  `id_notification` varchar(255) NOT NULL,
  `date_heure` datetime(6) DEFAULT NULL,
  `message` varchar(255) DEFAULT NULL,
  `statut` varchar(255) DEFAULT NULL,
  `typeCanal` varchar(255) DEFAULT NULL,
  `dateNotification` datetime(6) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `notifications`
--

INSERT INTO `notifications` (`id_notification`, `date_heure`, `message`, `statut`, `typeCanal`, `dateNotification`) VALUES
('20bd4d06-aa7c-4702-a23b-e2f897805fe8', '2026-03-27 02:22:12.417948', 'CONFLIT : La salle A302 a été libérée par l\'administrateur.', 'NON_LU', 'SYSTEME', NULL),
('3be51c1a-c6cc-4d7c-beec-c923124f1df9', '2026-03-27 02:22:15.015030', 'CONFLIT : La salle B101 a été libérée par l\'administrateur.', 'NON_LU', 'SYSTEME', NULL),
('5d0f2544-129a-47f3-88d2-3879e60982f7', '2026-03-31 10:15:40.640879', 'Votre réservation a été validée avec succès : Cours L2 PC | Salle A305 | 12:00-14:00', 'LU', 'EMAIL', NULL),
('72600af5-d004-4dbb-bffc-301cd648e7fd', '2026-04-13 10:48:17.524121', 'Votre réservation a été validée avec succès : Cours [Algèbre] validé.', 'LU', 'EMAIL', NULL),
('75833ef3-ad7e-43cd-8aa2-ed2eafb293f1', '2026-03-27 03:19:21.354734', 'Votre réservation a été validée avec succès : Cours L2 infos | Salle A302 | 08:00-10:00', 'LU', 'EMAIL', NULL),
('7ba29e09-072a-488b-9f56-d9c3bff92ea3', '2026-04-15 23:50:54.588284', 'Votre réservation a été validée avec succès : Cours [L2 INFOS] — Salle A3 — 11:00 - 13:00', 'LU', 'EMAIL', NULL),
('83dbfc28-b99d-4dfb-8eb5-d158eab52cb1', '2026-04-13 12:52:29.036712', 'Votre réservation a été validée avec succès : Cours [L3 infos] validé.', 'LU', 'EMAIL', NULL),
('9512a705-6dac-40cc-a12b-bbaea4159a56', '2026-03-27 02:15:27.801480', 'Votre réservation a été validée avec succès : Réservation immédiate salle A302', 'LU', 'EMAIL', NULL),
('a4f982ae-0ef4-4f75-8959-bb1e059b3624', '2026-04-17 02:03:51.645863', 'Votre réservation a été validée avec succès : Cours [SE] — Salle B50 — 08:00 - 10:00', 'LU', 'EMAIL', NULL),
('a6f159a9-c1d5-4c3f-bd91-8333586ebd62', '2026-03-27 02:40:50.992765', 'CONFLIT : La salle A302 est de nouveau LIBRE.', 'NON_LU', 'SYSTEME', NULL),
('ab259aad-9e0e-4a67-a292-9a87d4df8694', '2026-04-13 10:58:36.186019', 'Votre réservation a été validée avec succès : Confirmation UIDT : Salle A2', 'LU', 'EMAIL', NULL),
('ace07c3d-1fb6-4e00-b265-68e42516d3c5', '2026-03-27 02:04:36.318023', 'Démarrage du système UNIV-SCHEDULER', 'LU', 'SYSTEME', NULL),
('b9e96ffa-797d-4aed-b043-fd64f2aac14a', '2026-04-13 10:24:55.841713', 'Votre réservation a été validée avec succès : Confirmation UIDT : Salle B50 réservée.', 'LU', 'EMAIL', NULL),
('c77f8285-88e7-4682-a848-31ce72034c3f', '2026-03-27 03:10:30.619027', 'Votre réservation a été validée avec succès : Cours L2 INFO en Salle A302', 'LU', 'EMAIL', NULL),
('d3ddbc30-e2c7-429b-ac3f-1dd50e6a8fc1', '2026-03-27 03:17:31.279114', 'Votre réservation a été validée avec succès : Cours L2 info | Salle A302 | 08:00-10:00', 'LU', 'EMAIL', NULL),
('d5babc57-fb1a-4cde-a9e7-7ad12bb77b90', '2026-04-13 01:53:48.089864', 'Votre réservation a été validée avec succès : Confirmation UIDT : Le cours de DevWeb est réservé en Salle A6 de 12:00 à 15:00', 'LU', 'EMAIL', NULL),
('db959577-9404-4aad-947e-7190f206dcab', '2026-03-27 03:11:02.559087', 'Votre réservation a été validée avec succès : Cours L2 en Salle A302', 'LU', 'EMAIL', NULL),
('e81acaaa-41b3-4864-ac31-ef8ae80e9e9e', '2026-03-27 02:16:13.773136', 'Votre réservation a été validée avec succès : Réservation immédiate salle B101', 'LU', 'EMAIL', NULL),
('ef89187f-3d50-43c1-aaf7-632f2440503b', '2026-03-27 02:40:20.284146', 'Votre réservation a été validée avec succès : Réservation confirmée pour la salle A302', 'LU', 'EMAIL', NULL);

-- --------------------------------------------------------

--
-- Structure de la table `promotions`
--

CREATE TABLE `promotions` (
  `anneeAcademique` varchar(255) DEFAULT NULL,
  `codePromotion` varchar(255) NOT NULL,
  `code_filiere` varchar(255) DEFAULT NULL,
  `niveau` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `reservations`
--

CREATE TABLE `reservations` (
  `niveauPriorite` int(11) NOT NULL,
  `numReservation` int(11) NOT NULL,
  `dateDernierModification` datetime(6) DEFAULT NULL,
  `dateHeureReservation` datetime(6) DEFAULT NULL,
  `dernierModificateur` varchar(255) DEFAULT NULL,
  `etatReservation` varchar(255) DEFAULT NULL,
  `id_creneau` varchar(255) DEFAULT NULL,
  `motifReservation` varchar(255) DEFAULT NULL,
  `natureSession` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `salles`
--

CREATE TABLE `salles` (
  `capacite` int(11) NOT NULL,
  `categorieSalle` varchar(255) DEFAULT NULL,
  `code_batiment` varchar(255) DEFAULT NULL,
  `etatSalle` varchar(255) DEFAULT NULL,
  `numeroSalle` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `salles`
--

INSERT INTO `salles` (`capacite`, `categorieSalle`, `code_batiment`, `etatSalle`, `numeroSalle`) VALUES
(30, 'Laboratoire', 'A', 'Occupée', 'A1'),
(25, 'Standard', 'A', 'Disponible', 'A2'),
(40, 'Standard', 'A', 'Occupée', 'A3'),
(60, 'Standard', 'A', 'Disponible', 'A4'),
(15, 'Standard', 'A', 'Disponible', 'A5'),
(30, 'Standard', 'A', 'Disponible', 'A6'),
(100, 'Amphitheatre', 'B', 'Occupée', 'B50'),
(50, 'TD', 'BAT-C', 'Disponible', 'S19'),
(28, 'TP', 'BAT-Z', 'Disponible', 'Z1');

-- --------------------------------------------------------

--
-- Structure de la table `statistiques`
--

CREATE TABLE `statistiques` (
  `date` date DEFAULT NULL,
  `nombreAnnulations` int(11) NOT NULL,
  `nombreReservations` int(11) NOT NULL,
  `totalConflitsEvites` int(11) NOT NULL,
  `idStatistique` varchar(255) NOT NULL,
  `id_salle_moins_utilisee` varchar(255) DEFAULT NULL,
  `id_salle_plus_utilisee` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `ufrs`
--

CREATE TABLE `ufrs` (
  `codeUFR` varchar(255) NOT NULL,
  `libelleUFR` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `ufrs`
--

INSERT INTO `ufrs` (`codeUFR`, `libelleUFR`) VALUES
('2S', 'Sciences de la Sante'),
('SET', 'UFR Sciences et Technologies - Iba Der Thiam');

-- --------------------------------------------------------

--
-- Structure de la table `utilisateurs`
--

CREATE TABLE `utilisateurs` (
  `identifiantConnexion` varchar(255) NOT NULL,
  `nom` varchar(255) DEFAULT NULL,
  `prenom` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  `motDePasse` varchar(255) DEFAULT NULL,
  `DTYPE` varchar(31) NOT NULL,
  `chargeHoraireAnnuelle` double DEFAULT NULL,
  `grade` varchar(255) DEFAULT NULL,
  `idEnseignant` varchar(255) DEFAULT NULL,
  `specialite` varchar(255) DEFAULT NULL,
  `cycle` varchar(255) DEFAULT NULL,
  `ineEtudiant` varchar(255) DEFAULT NULL,
  `niveauEtude` varchar(255) DEFAULT NULL,
  `idGestionnaire` int(11) DEFAULT NULL,
  `responsabilite` varchar(255) DEFAULT NULL,
  `idAdmin` varchar(255) DEFAULT NULL,
  `code_groupe` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `utilisateurs`
--

INSERT INTO `utilisateurs` (`identifiantConnexion`, `nom`, `prenom`, `email`, `role`, `motDePasse`, `DTYPE`, `chargeHoraireAnnuelle`, `grade`, `idEnseignant`, `specialite`, `cycle`, `ineEtudiant`, `niveauEtude`, `idGestionnaire`, `responsabilite`, `idAdmin`, `code_groupe`) VALUES
('1976', 'Niang', 'Ousmane', 'elhadjimalicksy18@gmail.com', 'Enseignant', 'Flick2004@', 'Utilisateur', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
('197676', 'Ngom', 'Moussa', 'elhadjimalicksy18@gmail.com', 'Etudiant', 'Flick2004@', 'Utilisateur', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
('2004', 'Sy', 'El Malick', 'elhadjimalicksy18@gmail.com', 'Gestionnaire', 'Flick2004@', 'Utilisateur', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
('201', 'Ndiaye', 'Aly', 'alindiaye@gmail.com', 'Etudiant', 'aly201', 'Utilisateur', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
('24030100031', 'Sy', 'El Hadji Malick', 'elmalick25formcybersecu@gmail.com', 'Administrateur', 'Flick2004@', 'Utilisateur', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `administrateurs`
--
ALTER TABLE `administrateurs`
  ADD PRIMARY KEY (`identifiantConnexion`);

--
-- Index pour la table `batiments`
--
ALTER TABLE `batiments`
  ADD PRIMARY KEY (`code_batiment`);

--
-- Index pour la table `cours`
--
ALTER TABLE `cours`
  ADD PRIMARY KEY (`codeCours`);

--
-- Index pour la table `creneaux`
--
ALTER TABLE `creneaux`
  ADD PRIMARY KEY (`idCreneau`),
  ADD KEY `FKc14q2qfov6lq4pteg1d6i00df` (`code_cours`),
  ADD KEY `FKf48dpgdiypo45nwqw8vjtbh0u` (`numero_salle`);

--
-- Index pour la table `departements`
--
ALTER TABLE `departements`
  ADD PRIMARY KEY (`codeDepartement`),
  ADD KEY `FKthc1tx2dpovsar30ut8d6nx99` (`code_ufr`);

--
-- Index pour la table `enseignants`
--
ALTER TABLE `enseignants`
  ADD PRIMARY KEY (`identifiantConnexion`);

--
-- Index pour la table `equipements`
--
ALTER TABLE `equipements`
  ADD PRIMARY KEY (`idEquipement`),
  ADD KEY `FK1dm5oh10mt9h0rbi3q47wp0bp` (`numero_salle`);

--
-- Index pour la table `etudiants`
--
ALTER TABLE `etudiants`
  ADD PRIMARY KEY (`identifiantConnexion`),
  ADD KEY `FKtbt4rl8vs1qjxy2y5wpa1ifvt` (`code_groupe`);

--
-- Index pour la table `filieres`
--
ALTER TABLE `filieres`
  ADD PRIMARY KEY (`codeFiliere`),
  ADD KEY `FKgfphksvhkshhklwlsnfo4j8pe` (`code_departement`);

--
-- Index pour la table `gestionnaires`
--
ALTER TABLE `gestionnaires`
  ADD PRIMARY KEY (`identifiantConnexion`);

--
-- Index pour la table `groupes`
--
ALTER TABLE `groupes`
  ADD PRIMARY KEY (`codeGroupe`),
  ADD KEY `FKo734hws42venq25d95pg4533l` (`code_promotion`);

--
-- Index pour la table `incidents`
--
ALTER TABLE `incidents`
  ADD PRIMARY KEY (`idIncident`),
  ADD KEY `FK42313lmmuuorhjfgit4rql8wu` (`id_equipement`),
  ADD KEY `FK51dplx25p7p8kp964aujbvgdc` (`numero_salle`),
  ADD KEY `FKc8sjignw1vhhgll5miuy3rmhp` (`id_enseignant`);

--
-- Index pour la table `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`id_notification`);

--
-- Index pour la table `promotions`
--
ALTER TABLE `promotions`
  ADD PRIMARY KEY (`codePromotion`),
  ADD KEY `FKevkjkfdgtuuahxdtlmvncsrxn` (`code_filiere`);

--
-- Index pour la table `reservations`
--
ALTER TABLE `reservations`
  ADD PRIMARY KEY (`numReservation`),
  ADD UNIQUE KEY `UK_ty7s4wp0xhstjc6axbpo33h0` (`id_creneau`);

--
-- Index pour la table `salles`
--
ALTER TABLE `salles`
  ADD PRIMARY KEY (`numeroSalle`),
  ADD KEY `FKn55rhe9q24te64get4nuxc0p7` (`code_batiment`);

--
-- Index pour la table `statistiques`
--
ALTER TABLE `statistiques`
  ADD PRIMARY KEY (`idStatistique`),
  ADD KEY `FKdpiq0pxe8dy6aeigdhp5d15m` (`id_salle_moins_utilisee`),
  ADD KEY `FKhd0p4662ox3rt04nrklbe6w7l` (`id_salle_plus_utilisee`);

--
-- Index pour la table `ufrs`
--
ALTER TABLE `ufrs`
  ADD PRIMARY KEY (`codeUFR`);

--
-- Index pour la table `utilisateurs`
--
ALTER TABLE `utilisateurs`
  ADD PRIMARY KEY (`identifiantConnexion`),
  ADD KEY `FK87rpq6sprcclhcvdt5plgtkla` (`code_groupe`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `incidents`
--
ALTER TABLE `incidents`
  MODIFY `idIncident` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `reservations`
--
ALTER TABLE `reservations`
  MODIFY `numReservation` int(11) NOT NULL AUTO_INCREMENT;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `administrateurs`
--
ALTER TABLE `administrateurs`
  ADD CONSTRAINT `FKnlxl4atvrgr3x0y52lav44uq9` FOREIGN KEY (`identifiantConnexion`) REFERENCES `utilisateurs` (`identifiantConnexion`);

--
-- Contraintes pour la table `creneaux`
--
ALTER TABLE `creneaux`
  ADD CONSTRAINT `FKc14q2qfov6lq4pteg1d6i00df` FOREIGN KEY (`code_cours`) REFERENCES `cours` (`codeCours`),
  ADD CONSTRAINT `FKf48dpgdiypo45nwqw8vjtbh0u` FOREIGN KEY (`numero_salle`) REFERENCES `salles` (`numeroSalle`);

--
-- Contraintes pour la table `departements`
--
ALTER TABLE `departements`
  ADD CONSTRAINT `FKthc1tx2dpovsar30ut8d6nx99` FOREIGN KEY (`code_ufr`) REFERENCES `ufrs` (`codeUFR`);

--
-- Contraintes pour la table `enseignants`
--
ALTER TABLE `enseignants`
  ADD CONSTRAINT `FKjcyc13efwepeu4g413up6px3i` FOREIGN KEY (`identifiantConnexion`) REFERENCES `utilisateurs` (`identifiantConnexion`);

--
-- Contraintes pour la table `equipements`
--
ALTER TABLE `equipements`
  ADD CONSTRAINT `FK1dm5oh10mt9h0rbi3q47wp0bp` FOREIGN KEY (`numero_salle`) REFERENCES `salles` (`numeroSalle`);

--
-- Contraintes pour la table `etudiants`
--
ALTER TABLE `etudiants`
  ADD CONSTRAINT `FKi3uu3nh7ppi00cao8bbya95lm` FOREIGN KEY (`identifiantConnexion`) REFERENCES `utilisateurs` (`identifiantConnexion`),
  ADD CONSTRAINT `FKtbt4rl8vs1qjxy2y5wpa1ifvt` FOREIGN KEY (`code_groupe`) REFERENCES `groupes` (`codeGroupe`);

--
-- Contraintes pour la table `filieres`
--
ALTER TABLE `filieres`
  ADD CONSTRAINT `FKgfphksvhkshhklwlsnfo4j8pe` FOREIGN KEY (`code_departement`) REFERENCES `departements` (`codeDepartement`);

--
-- Contraintes pour la table `gestionnaires`
--
ALTER TABLE `gestionnaires`
  ADD CONSTRAINT `FKa3pcxq9t9qkdyc2c830k7pd1e` FOREIGN KEY (`identifiantConnexion`) REFERENCES `utilisateurs` (`identifiantConnexion`);

--
-- Contraintes pour la table `groupes`
--
ALTER TABLE `groupes`
  ADD CONSTRAINT `FKo734hws42venq25d95pg4533l` FOREIGN KEY (`code_promotion`) REFERENCES `promotions` (`codePromotion`);

--
-- Contraintes pour la table `incidents`
--
ALTER TABLE `incidents`
  ADD CONSTRAINT `FK42313lmmuuorhjfgit4rql8wu` FOREIGN KEY (`id_equipement`) REFERENCES `equipements` (`idEquipement`),
  ADD CONSTRAINT `FK51dplx25p7p8kp964aujbvgdc` FOREIGN KEY (`numero_salle`) REFERENCES `salles` (`numeroSalle`),
  ADD CONSTRAINT `FKc8sjignw1vhhgll5miuy3rmhp` FOREIGN KEY (`id_enseignant`) REFERENCES `utilisateurs` (`identifiantConnexion`),
  ADD CONSTRAINT `FKkxie5apkw67dsegah578xsppe` FOREIGN KEY (`id_enseignant`) REFERENCES `enseignants` (`identifiantConnexion`);

--
-- Contraintes pour la table `promotions`
--
ALTER TABLE `promotions`
  ADD CONSTRAINT `FKevkjkfdgtuuahxdtlmvncsrxn` FOREIGN KEY (`code_filiere`) REFERENCES `filieres` (`codeFiliere`);

--
-- Contraintes pour la table `reservations`
--
ALTER TABLE `reservations`
  ADD CONSTRAINT `FKildxnp067ugbjtj244yrlq495` FOREIGN KEY (`id_creneau`) REFERENCES `creneaux` (`idCreneau`);

--
-- Contraintes pour la table `salles`
--
ALTER TABLE `salles`
  ADD CONSTRAINT `FKn55rhe9q24te64get4nuxc0p7` FOREIGN KEY (`code_batiment`) REFERENCES `batiments` (`code_batiment`);

--
-- Contraintes pour la table `statistiques`
--
ALTER TABLE `statistiques`
  ADD CONSTRAINT `FKdpiq0pxe8dy6aeigdhp5d15m` FOREIGN KEY (`id_salle_moins_utilisee`) REFERENCES `salles` (`numeroSalle`),
  ADD CONSTRAINT `FKhd0p4662ox3rt04nrklbe6w7l` FOREIGN KEY (`id_salle_plus_utilisee`) REFERENCES `salles` (`numeroSalle`);

--
-- Contraintes pour la table `utilisateurs`
--
ALTER TABLE `utilisateurs`
  ADD CONSTRAINT `FK87rpq6sprcclhcvdt5plgtkla` FOREIGN KEY (`code_groupe`) REFERENCES `groupes` (`codeGroupe`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
