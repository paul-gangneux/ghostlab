# Ghostlab

## À propos

Ce projet est un couple client / serveur pour un jeu multijoueur dont le but est de capturer des fantômes dans un labyrinthe. Il a été programmé à l'occasion d'un projet scolaire de 3eme année de Licence, pour la matière Programmation Réseau.

Le sujet de notre projet est décrit dans le fichier `projet-reseau-22.pdf`

Progammé par Paul Gangneux, Ulysse Dupont Latapie, et Yani Akli Bouanem.

## Compilation

Le projet se compile entièrement (serveur et client) en lançant l'exécutable `./build.sh` à la racine du dépôt.
Il faut rendre le script executable avec `chmod u+x build.sh`.

`./build.sh -C` permet de supprimer les fichiers compilés.

## Lancement du serveur

Le serveur se lance avec l'exécutable `server.out` à la racine du dépôt.

Un port de connexion peut être passé en ligne de commande : `./server.out [-p <port>]`, le port par défaut est 4242.
L' option `-h` détaille toutes les options disponibles.

## Lancement du client

Le client se lance en exécutant l'archive jar `client.jar` à la racine :
`./client.jar [-a <adresse IP ou nom de machine>] [-p <port>]`
ou `java -jar client.jar [-a <adresse IP ou nom de machine>] [-p <port>]`

L'adresse par défaut est `localhost` (`127.0.0.1`) et le port par défaut est `4242`.
L'option `-h` détaille toutes les options disponibles.

## Utilisation

Une fois le serveur lancé, il tourne jusqu'à ce qu'il reçoit `quit\n` sur son entrée standard, ou son interruption par un signal.

Le client doit être lancé après le serveur. une fois le client lancé, les interactions se font avec la souris, sauf pour entrer du texte dans des champs.

## Architecture

Le serveur a été fait en C, chaque joueur et chaque jeu a sa structure associée. Un thread s'occupe d'accépter les connexions client, il y a un thread par client, et un thread par partie commencée qui s'occupera de bouger les fantômes à intervalles réguliers.

`server.c` contient la fonction main, `communication.c` s'occupe de gérer les fonctions de récéption / envoi de message, `maze.c` génère un labyrinthe, `game.c` et `player.c` possèdent les fonctions manipulant les structures du même nom.

Le client a été fait en Java. Il y a un Thread par socket de communication (un pour l'écoute TCP, un pour l'écoute UDP, et un pour l'écoute Multicast), et un thread pricipal depuis lequel seront envoyé les requêtes TCP.

le package `launcher` contient la classe principale qui lance le programme, les fonctions de communication réseau sont dans le package `client`, le package `model` contient des classes utiles pour modéliser le jeu, et le package `ui` contient tout ce qui est lié à l'interface graphique, notemment `View.java` qui est la classe principale de l'ig.