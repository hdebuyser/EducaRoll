import extensions.CSVFile;

class EducaRoll extends Program{

    // Constantes définies pour le fichier de questions, le nombre de tours, et le nombre de faces du dé
    final String FICHIER_QUESTION = "Questionnaire.csv";
    final String PLATEAU = "Plateau.csv";
    final int NB_TOURS = 10;
    final int NB_FACE_DE = 6;
    final int LONGUEUR_PLATEAU = 10;
    final int LARGEUR_PLATEAU = 10;
    final int MAX_PLAYEUR = 4;
    final Couleur[] Color = new Couleur[]{Couleur.RED, Couleur.GREEN, Couleur.YELLOW, Couleur.BLUE, Couleur.PURPLE, Couleur.CYAN, Couleur.WHITE};
    final String [] tab_Des = new String[]{" --------- \n |      | \n |   0   | \n |       | \n ---------\n"," --------- \n |    0  | \n |       | \n |  0    | \n ---------\n ", " --------- \n |     0 | \n |   0   | \n | 0     | \n ---------\n" ," ---------  \n | 0   0 | \n |       | \n | 0   0 | \n ---------\n "," --------- \n | 0   0 | \n |   0   | \n | 0   0 | \n ---------\n"," --------- \n | 0   0 | \n | 0   0 | \n | 0   0 | \n ---------\n"};
    
    boolean quitter = false;

    // Méthode pour afficher le menu et obtenir le mode de jeu (nombre de joueurs)
    int Menu(){
        background("black");
        text("white");
        clearScreen();
        println("____EducaRoll____");
        println();
        println("----Bienvenue sur EducaRoll !-----");
        println();
        println("/////////////////////// \n//// Mode de Jeu : //// \n///////////////////////");
        println("   Selectionner le mode de jeu :");
        int mode = 0;
        do{
            println("1) Mode Questions 2) Mode Plateau 3) Quitter");
            mode = readInt();
            if(mode<1 || mode>3){
                println("choissisez le mode de jeu 1 ou 2 et 3 pour quitter");
            }
        }while(mode<1 || mode>3);
        return mode;
    }

    int randomDe(){
        return (int) (random()*NB_FACE_DE)+1;
    }

    Question newQuestion(String type, String matiere, String question, String reponse, String justification){
        Question renvoie = new Question();
        renvoie.type = type;
        renvoie.matiere = matiere;
        renvoie.question = question;
        renvoie.reponse = reponse;
        renvoie.justification = justification;
        return renvoie;
    }

    Question loadQuestion(String nomFichier, String faceDe){
        CSVFile QuestionCSV = loadCSV(nomFichier);
        int idx = (int)((random()*rowCount(QuestionCSV)));
        while(equals(getCell(QuestionCSV, idx, 0), faceDe)==false){
            idx = (int)((random()*rowCount(QuestionCSV)));
        }
        return newQuestion(getCell(QuestionCSV, idx, 0), getCell(QuestionCSV, idx, 1), getCell(QuestionCSV, idx, 2), getCell(QuestionCSV, idx, 3), getCell(QuestionCSV, idx, 4));
    }

    void checkAnswer(String reponse, Question question, Joueur joueur){
        println();
        clearScreen();
        if(equals(toUpperCase(reponse), toUpperCase(question.reponse))){
            joueur.NbBonneReponse = joueur.NbBonneReponse+1;
            text("green");
            up(LONGUEUR_PLATEAU);
            println("Bonne réponse !");
            text("white");
            delay(1000);
            clearScreen();
        }
        else{
            up(LONGUEUR_PLATEAU);
            text("red");
            println("Mauvaise réponse !");
            text("white");
            println(question.justification);
            println("Appuyez sur 'Entrée' pour continuer");
            String vide = readString();
            clearScreen();
        }
    }

    boolean rejouer(String c){
        if(equals(toUpperCase(c), "N")){
            return false;
        }
        return true;
    }

    Joueur newJoueur(int numJoueur) {
        print("Entrez le nom du Joueur " + numJoueur + " : ");
        String nom = readString();
        println();
        Joueur joueur = new Joueur();
        joueur.nom = nom;
        joueur.numero = numJoueur;
        joueur.positionX = LONGUEUR_PLATEAU-1;
        joueur.positionY = 0;
        if(numJoueur<=length(Color)){
            joueur.color = Color[numJoueur - 1];
        }
        return joueur;
    }

    String playTurn(Joueur joueur){
        String face = "" + randomDe();
        println(tab_Des[stringToInt(face)-1]);
        Question selectedQuestion = loadQuestion(FICHIER_QUESTION, face);
        println("Question " + selectedQuestion.matiere);
        println("");
        print(selectedQuestion.question + " ");
        checkAnswer(readString(), selectedQuestion, joueur);
        return face;
    }

    void PlayGame(int nbJoueur, int mode){
        Joueur[] joueurs = new Joueur [nbJoueur];
        for(int i = 0; i<nbJoueur;i++){
            joueurs[i] = newJoueur(i+1);
        }
        if(mode == 1){
            QuestionMode(joueurs);
        }
        else if(mode == 2){
            PlatoMode(joueurs);
        }
    }

    void PlatoMode(Joueur[] joueurs) {
        Plateau Plate = new Plateau();
        Plate.longueur = LONGUEUR_PLATEAU;
        Plate.largeur = LARGEUR_PLATEAU;
        Plate.position = new Case[Plate.longueur][Plate.largeur];
        FillPlato(Plate, joueurs);
        clearScreen();
        up(LONGUEUR_PLATEAU/2);
        DisplayPlato(Plate, joueurs);
        boolean gameFinished = false;
        while (!gameFinished) {
            for (int i = 0; i < length(joueurs); i++) {
                Joueur currentPlayer = joueurs[i];
                text(toLowerCase(currentPlayer.color.name()));
                down(2);
                print(currentPlayer.nom);
                text("white");
                println(", c'est à votre tour !");

                Obstacle obstacle = getObstacleAtPosition(Plate.obstacles, currentPlayer.positionX, currentPlayer.positionY);

                if (obstacle != null && (currentPlayer.positionX == obstacle.queueX && currentPlayer.positionY == obstacle.queueY)) {
                handleObstacle(currentPlayer, Plate, obstacle);
                } else {
                    int bonneReponsesAvant = currentPlayer.NbBonneReponse;
                    println("Appuyez sur 'Entrée' pour lancer le dé");
                    String vide = readString();
                    String faceDe = playTurn(currentPlayer);

                    if (currentPlayer.NbBonneReponse > bonneReponsesAvant) {
                        movePlayer(currentPlayer, Plate, faceDe);

                        if (currentPlayer.positionX == 0 && currentPlayer.positionY == LARGEUR_PLATEAU-1) {
                            up(LONGUEUR_PLATEAU/2);
                            println(currentPlayer.nom + " a gagné ! Fin de la partie.");
                            println("Appuyer sur 'Entrée' pour revenir au menu principal.");
                            vide = readString();
                            gameFinished = true;
                            break;
                        }
                    }
                    else{
                        int negatif = stringToInt(faceDe)-(stringToInt(faceDe)*2);
                        faceDe = ""+negatif;
                        movePlayer(currentPlayer, Plate, faceDe);
                    }
                }
                DisplayPlato(Plate, joueurs);
                println();
            }
        }
    }

    Obstacle getObstacleAtPosition(Obstacle[] obstacles, int x, int y) {
        for (Obstacle obstacle : obstacles) {
            if (obstacle != null && obstacle.teteX == x && obstacle.teteY == y) {
                return obstacle;
            } else if (obstacle != null && obstacle.queueX == x && obstacle.queueY == y) {
                return obstacle;
            }
        }
        return null;
    }

    void handleObstacle(Joueur joueur, Plateau plateau, Obstacle obstacle) {
        println("");
        if(obstacle.type == "Serpent"){
            text("red");
            print("Attention, vous êtes sur le Serpent de couleur ");
            text(toLowerCase(obstacle.color.name()));
            print(toLowerCase(obstacle.color.name()));
            println(" !");
        }
        else if(obstacle.type == "Echelle"){
            text("green");
            print("Vous êtes sur l'Echelle de couleur ");
            text(toLowerCase(obstacle.color.name()));
            print(toLowerCase(obstacle.color.name()));
            println(" !");
        }
        text("white");
        println("");

        if (equals(obstacle.type, "Serpent")) {
            int bonneReponsesAvant = joueur.NbBonneReponse;
            String faceDe = playTurn(joueur);

            if (joueur.NbBonneReponse > bonneReponsesAvant) {
                movePlayer(joueur, plateau, faceDe);
            } else {
                joueur.positionX = obstacle.teteX;
                joueur.positionY = obstacle.teteY;
                println("Dommage ! Vous vous êtes déplacé à la tête du " + obstacle.type + ".");
            }
        } else if (equals(obstacle.type, "Echelle")) {
            int bonneReponsesAvant = joueur.NbBonneReponse;
            String faceDe = playTurn(joueur);

            if (joueur.NbBonneReponse > bonneReponsesAvant) {
                joueur.positionX = obstacle.teteX;
                joueur.positionY = obstacle.teteY;
                println("Bravo ! Vous vous êtes déplacé à la tête de l'" + obstacle.type + ".");
            }
        }
    }

    void FillPlato(Plateau plateau, Joueur[] joueurs) {
        for (int idx_long = 0; idx_long < plateau.longueur; idx_long++) {
            for (int idx_larg = 0; idx_larg < plateau.largeur; idx_larg++) {
                plateau.position[idx_long][idx_larg] = new Case();
                plateau.position[idx_long][idx_larg].symbole = ".";
                plateau.position[idx_long][idx_larg].type = "vide";
            }
        }
        int nbSerpent = length(Color);
        int nbEchelle = length(Color);
        plateau.obstacles = new Obstacle[nbSerpent+nbEchelle];
        for (int i = 0; i < nbSerpent; i++) {
            plateau.obstacles[i] = createObstacle(plateau, "Serpent", i + 1);
        }
        for (int i = nbSerpent; i < nbSerpent+nbEchelle; i++) {
            plateau.obstacles[i] = createObstacle(plateau, "Echelle", i-nbSerpent + 1);
        }
        for (Joueur joueur : joueurs) {
            plateau.position[joueur.positionX][joueur.positionY].type = "joueur";
        }
    }

    Obstacle createObstacle(Plateau plateau, String type, int numero) {
        Obstacle obstacle = new Obstacle();
        obstacle.type = type;
        obstacle.numero = numero;

        if (equals(type, "Serpent")) {
            obstacle.color = Color[numero - 1];
            do {
                obstacle.teteX = (int) (random() * (plateau.longueur-plateau.longueur/4)) + plateau.longueur/5;
                obstacle.teteY = (int) (random() * (plateau.largeur-2)) + 1;
                obstacle.queueX = (int) (random() * obstacle.teteX/1.5);
                obstacle.queueY = (int) (random() * (plateau.largeur-2)) + 1;
            } while (positionOccupied(plateau.obstacles, obstacle));
        }

        if (equals(type, "Echelle")) {
            obstacle.color = Color[length(Color) - numero];
            do {
                obstacle.teteX = (int) (random() * (plateau.longueur-1)/2);
                obstacle.teteY = (int) (random() * (plateau.largeur-2)) + 1;
                obstacle.queueX = (int) (random() * (plateau.longueur - obstacle.teteX - 1)) + obstacle.teteX + 1;
                obstacle.queueY = (int) (random() * (plateau.largeur-2)) + 1;
            } while (positionOccupied(plateau.obstacles, obstacle) || obstacle.teteX == obstacle.queueX);
        }
        return obstacle;
    }

    boolean positionOccupied(Obstacle[] obstacles, Obstacle obstacle) {
        for (Obstacle other : obstacles) {
            if (other != null &&
                ((obstacle.teteX == other.teteX && obstacle.teteY == other.teteY) ||
                (obstacle.queueX == other.queueX && obstacle.queueY == other.queueY) ||
                (obstacle.teteX == other.queueX && obstacle.teteY == other.queueY) ||
                (obstacle.queueX == other.teteX && obstacle.queueY == other.teteY))) {
                return true;
            }
        }
        return false;
    }

    void DisplayPlato(Plateau Plate, Joueur[] joueurs) {
        /* clearScreen(); */
        for (int idx = 0; idx < Plate.longueur; idx++) {
            for (int jdx = 0; jdx < Plate.largeur; jdx++) {
                printObstacle(Plate.obstacles, joueurs, idx, jdx);
            }
            text("white");
            background("black");
            println("");
        }
    }

    void printObstacleSymbol(String type, int numero, boolean isHead) {
        if (equals(type, "Serpent")) {
            if (isHead) {
                print("S ");
            } else {
                print("s ");
            }
        } else if (equals(type, "Echelle")) {
            if (isHead) {
                print("E ");
            } else {
                print("e ");
            }
        }
    }

    void printObstacle(Obstacle[] obstacles, Joueur[] joueurs, int x, int y) {
        for (Joueur joueur : joueurs) {
            if (joueur.positionX == x && joueur.positionY == y) {
                background(toLowerCase(joueur.color.name()));
                print(joueur.numero + " ");
                background("black");
                return;
            }
        }
        for (Obstacle obstacle : obstacles) {
            text(toLowerCase(obstacle.color.name()));
            if (x == obstacle.teteX && y == obstacle.teteY) {
                printObstacleSymbol(obstacle.type, obstacle.numero, true);
                return;
            } else if (x == obstacle.queueX && y == obstacle.queueY) {
                printObstacleSymbol(obstacle.type, obstacle.numero, false);
                return;
            }
        }
        text("white");
        background("black");
        print(". ");
    }

    void movePlayer(Joueur joueur, Plateau plateau, String faceDe) {
        plateau.position[joueur.positionX][joueur.positionY].type = "vide";

        int numberOfCases = stringToInt(faceDe);
        int newY;

        if (numberOfCases > 0) {
            newY = (joueur.positionY + numberOfCases) % plateau.largeur;

            if (joueur.positionY + numberOfCases >= plateau.largeur) {
                if (joueur.positionX == 0) {
                    newY = plateau.longueur - 1;
                } else {
                    int newRow = (joueur.positionX - 1) % plateau.longueur;
                    joueur.positionX = newRow < 0 ? plateau.longueur - 1 : newRow;
                    newY = (joueur.positionY + numberOfCases) % plateau.largeur; 
                }
            }
        } else if (numberOfCases < 0) {
            newY = (joueur.positionY + numberOfCases + plateau.largeur) % plateau.largeur;

            if (joueur.positionY + numberOfCases < 0) {
                if (joueur.positionX == plateau.longueur - 1) {
                    newY = 0;
                } else {
                    int newRow = (joueur.positionX + 1) % plateau.longueur;
                    joueur.positionX = newRow;
                    newY = (joueur.positionY + numberOfCases + plateau.largeur) % plateau.largeur;
                }
            }
        } else {
            newY = joueur.positionY;
        }

        joueur.positionY = newY;
        joueur.positionX = Math.min(joueur.positionX, plateau.longueur - 1);
        plateau.position[joueur.positionX][joueur.positionY].type = "joueur";
    }


    void QuestionMode(Joueur[] joueurs){
        int numTours = 1;
        while(numTours<NB_TOURS+1){
            String faceDe = "0";
            for(int i = 0; i<length(joueurs); i++){
                println("Tours de " + joueurs[i].nom + " N° " + numTours);
                faceDe = playTurn(joueurs[i]);
            }
            numTours=numTours+1;
        }
        for(int i = 0; i<length(joueurs);i++){
            println(joueurs[i].nom + " a eu " + joueurs[i].NbBonneReponse + " bonnes réponses.");
        }
    }

    void algorithm(){
        while(quitter==false){
            int GameMode = Menu();
            if(GameMode!=3){
                int nbJoueur = 0;
                while(nbJoueur<=0){
                    print("Selectionner le nombre de joueurs (maximum "+ MAX_PLAYEUR +") : ");
                    nbJoueur = readInt();
                    println("");
                    if(nbJoueur>MAX_PLAYEUR){
                        println("Le nombre max de joueur est de " + MAX_PLAYEUR);
                        nbJoueur = 0;
                    }
                }
                PlayGame(nbJoueur, GameMode);
            }
            else{
                quitter = true;
            }
        }
    }

    /* void testPlateauPlacement() {
        for (int i = 0; i < 10; i++) {
            println("Test du plateau " + (i + 1) + ":");
            Plateau testPlateau = new Plateau();
            testPlateau.longueur = 10;
            testPlateau.largeur = 10;
            testPlateau.position = new Case[testPlateau.longueur][testPlateau.largeur];
            FillPlato(testPlateau);
            for (Obstacle obstacle : testPlateau.obstacles) {
                assertNotEquals(obstacle.teteX, obstacle.queueX);
                assertTrue(obstacle.teteX >= 0 && obstacle.teteX <= 9);
                assertTrue(obstacle.queueX >= 0 && obstacle.queueX <= 9);
                assertTrue(obstacle.teteY >= 0 && obstacle.teteY <= 9);
                assertTrue(obstacle.queueY >= 0 && obstacle.queueY <= 9);
                if (obstacle.type.equals("Serpent")) {
                    assertTrue(obstacle.teteX > obstacle.queueX);
                } else if (obstacle.type.equals("Echelle")) {
                    assertTrue(obstacle.teteX < obstacle.queueX);
                }
            }
        }
    } */
}