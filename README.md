### Compilation

Le projet se compile entièrement (serveur et client) en lançant l'exécutable `./build.sh` à la racine du dépôt.
Il faut rendre le scripte executable avec `chmod u+x build.sh`.

### Lancement du serveur

Le serveur se démarre en lançant l'exécutable `server.out` à la racine du dépôt.

Un port de connexion peut être passé en ligne de commande : `./server.out [-p <port>]`

Le serveur dispose également d'une option `-h` qui détaille les options disponibles.

Un mode verbeux (`-v`) ou très verbeux (`-V`) sont également disponibles.

### Lancement du client

Le client se lance en exécutant l'archive jar `client.jar` à la racine :

`./client.jar [-a <adresse IP ou nom de machine>] [-p <port>]`

ou `java -jar client.jar [-a <adresse IP ou nom de machine>] [-p <port>]`

L'adresse par défaut est `localhost` (`127.0.0.1`) et le port par défaut est `4242`.