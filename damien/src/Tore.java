//Pour les fenêtres
import java.awt.*;
import java.awt.event.*;

//Pour le jogl
import net.java.games.jogl.*;
import net.java.games.jogl.util.GLUT;

/** 
 * Portage de multiview.c de Nate Robins en jogl
 * On affiche un tore en fil de fer sous 4 vues différentes
 *
 * L'interêt est purement pédagogique
 */
public class Tore {

    // Frame définie ici et pas dans le main car la classe interne doit y
    // accéder pour pouvoir connaître la taille de la fenêtre
    static Frame frame; 


    /**
     * Note sur <b>GLEventListener</b> : utilisé pour rappeler nos composantes
     * graphiques si on doit les redéssiner ou modifier leur apparence
     *
     * L'interface définit 4 méthodes :
     * -<i>init (GLDrawable drawable)</i> : 
     *      appelée à l'initialisation de l'OpenGL (et donc utile pour les 
     *      cas où on dessine une seule fois)
     *
     * -<i>display (GLDrawable drawable)</i> : 
     *      pour que la composante se dessine elle-même
     *
     * -<i>reshape (GLDrawable drawable, 
                    int i, int x, int width, int height)</i> : 
     *      signale que la composante a changé de taille ou d'emplacement
     *
     * -<i>displayChanged (GLDrawable drawable, 
     *                     boolean modeChanged, 
     *                     boolean deviceChanged)</i> : 
     *      pour signaler que le display mode (résolution || nb de couleurs) a
     *      été changé (par l'utilisateur sans doute), ou que la fenêtre a été
     *      déplacée sur un autre écran <b> Non supporté pour le moment</b>
     *
     * NB : Au démarrage, on appelle init(), reshape(), puis display()
     *      Après : reshape() si changement de taille de fenêtre
     *              display() tout le temps
     */
    static class Renderer implements GLEventListener, KeyListener {

        // POUR DEPLACER LE TORE AU CLAVIER
        // Tableau stockant les touches du clavier pressées.
        boolean keys[] = new boolean[256];
        // Si on appuie sur les flèches gauche ou droite on modifiera spin_x
        float spin_x = 0.0f;
        // Si on appuie sur les flèches haut ou bas on modifiera spin_y
        float spin_y = 0.0f;
        // FIN CLAVIER

        //int  maxtokens = 0;
        int torus_list = 0;
        
        // Un Animator peut être attaché à un GLDrawable object pour gérer sa
        // méthode display() dans une boucle. Pour plus d'efficacité, l'Animator
        // fait que le thread de rendu (rendering) du GLDrawable object devient
        // son propre thread (donc on ne peut pas combiner
        // cette méthode avec des "repaint" manuels de la surface par ex)
        Animator loop;


        /** 
         * Pour initialiser le rendering OpenGL
         * <B>C'est quoi un GLDrawable object ?</B>
         * En gros c'est l'"interface" basique qui nous relie à OpenGL
         * (Apparement c'est un directement un bon gros dump de gl.h)
         * Le mieux c'est <i>de ne pas le voir comme un objet, mais juste comme une
         * sorte de handle pour faire des appels de méthodes</i>
         * En cas de portage depuis OpenGL, les fonctions commençant par gl et
         * les constantes commençant par GL sont accessibles grâce à cette
         * instance de la classe GL
         *
         * @param gLDrawable The GLDrawable object.
         */
        public void init(GLDrawable gLDrawable) {

            final GL gl = gLDrawable.getGL();
            final GLU glu = gLDrawable.getGLU();

            float[] light_pos = { 1.0f, 1.0f, 1.0f, 0.0f };

            //GL_LIGHT_MODEL_TWO_SIDE : pour notre tore en fil de fer on éclaire
            //la face normalement visible ("à l'extérieur du grand rayon") ET 
            // la face "interne" ("à l'extérieur du petit rayon")
            gl.glLightModeli(GL.GL_LIGHT_MODEL_TWO_SIDE, GL.GL_TRUE);

            // glLightfv(nb de lumière, type de source, 
            // On met une lumière : GL_LIGHT0 (0 indique la première lumière)
            // De type GL_POSITION : selon la position la lumière prend certains
            // paramètres spécifiques (elle est directionnelle apparement)
            gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, light_pos);
            gl.glEnable(GL.GL_LIGHTING);
            gl.glEnable(GL.GL_LIGHT0);
            gl.glEnable(GL.GL_DEPTH_TEST);
            
            // Si activé : on ne voit pas les faces cachées
            // Ici désactivé car en fil de fer c'est intéréssant de voir
            // l'intérieur du tore
            gl.glDisable(GL.GL_CULL_FACE);


            
            gLDrawable.addKeyListener(this);


            float[] gold_Ka = { 0.24725f, 0.1995f, 0.0745f, 1.0f };
            float[] gold_Kd = { 0.75164f, 0.60648f, 0.22648f, 1.0f };
            float[] gold_Ks = { 0.628281f, 0.555802f, 0.366065f, 1.0f };
            float gold_Ke    = 41.2f;
            float[] silver_Ka  = { 0.05f, 0.05f, 0.05f, 1.0f };
            float[] silver_Kd  = { 0.4f, 0.4f, 0.4f, 1.0f };
            float[] silver_Ks  = { 0.7f, 0.7f, 0.7f, 1.0f };
            float silver_Ke     = 12.0f;

            // Une liste de commandes stockées, qu'on peut appeler par un
            // glCallList()
            // glGenLists prend en paramètre le nombre de display liste vide
            // qu'on veut générer
            torus_list = gl.glGenLists(1);
                gl.glNewList(torus_list, GL.GL_COMPILE);
                    
                    // glMaterialf assigne des valeurs aux paramètres du
                    // "material" : il y a 2 jeux de paramètres, le premier est
                    // utilisé pour les points, lignes, bitmap, polygones ;
                    // l'autre sert pour les faces arrières si on utilise le
                    // "two-side lighting"
                    gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT,  gold_Ka);
                    gl.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE,  gold_Kd);
                    gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, gold_Ks);
                    gl.glMaterialf(GL.GL_FRONT, GL.GL_SHININESS, gold_Ke);
                    gl.glMaterialfv(GL.GL_BACK, GL.GL_AMBIENT,  silver_Ka);
                    gl.glMaterialfv(GL.GL_BACK, GL.GL_DIFFUSE,  silver_Kd);
                    gl.glMaterialfv(GL.GL_BACK, GL.GL_SPECULAR, silver_Ks);
                    gl.glMaterialf(GL.GL_BACK, GL.GL_SHININESS, silver_Ke);
                    GLUT zonk = new GLUT();
                    zonk.glutWireTorus(gl, 0.3, 0.5, 16, 32);
                gl.glEndList();
            

            //Créer et lancer un Animator est simple :
            //  on le construit avec un GLDisplayable argument
            //  puis on le démarre avec start()
            loop = new Animator(gLDrawable);
            loop.start();




            
        }




        /**
         * Pour qu'une composante puisse se dessiner
         * Appelée par la composante pour débuter le rendu OpenGL par le client
         */
        public void display(GLDrawable gLDrawable) {

            /**
             * NB : Le GL object est obtenu du GLDrawable object passé dans init()
             * Tous les GLEventListener fournissent cet objet, et le <i>JOGL User's
             * Guide</i> conseille de toujours prendre une référence récente vers le
             * GL object depuis le GLDrawable object à chaque appel, plutôt que de
             * le stocker pour le réutiliser (pb de thread dans AWT, mieux vaut
             * éviter un appel OpenGL depuis le mauvais thread)
             */
            final GL gl = gLDrawable.getGL();

            /**
             * De même, quand on doit faire un appel à glu.h, on prend le GLU
             * object depuis le GLDrawable object
             */
            final GLU glu = gLDrawable.getGLU();

            
            // On récupère la taille de la fenêtre pour que le viewport
            // occupe 100% de la fenêtre
            int width = (int)frame.getSize().getWidth();
            int height = (int)frame.getSize().getHeight();


            // On définit le viewport : zone dans laquelle on travaille
            // NB : En OpenGL l'origine (0,0) est en bas à gauche et pas en haut à
            // gauche comme sous AWT
            gl.glViewport(0, 0, width, height);

            // On spécifie quelle pile de matrice sera la cible pour les
            // opérations qui vont suivre. 3 valeurs possibles :
            //      GL_MODELVIEW, GL_PROJECTION, et GL_TEXTURE.
            gl.glMatrixMode(GL.GL_PROJECTION);

            // On remplace la matrice en sommet de pile par l'identité
            gl.glLoadIdentity();

            // Définit une matrice de projection orthographique 2D
            glu.gluOrtho2D(0, width, 0, height);

            // On s'adresse maintenant à la pile de matrice GL_MODELVIEW
            gl.glMatrixMode(GL.GL_MODELVIEW);

            // On remplace la matrice en sommet de pile par l'identité
            gl.glLoadIdentity();
            
            // On indique les valeurs rouge/vert/bleu/alpha à utiliser quand le
            // buffer des couleurs est vidé (0 0 0 0 par défaut) 
            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

            // On désactive GL_LIGHTING.
            // Si activé : on utilise les paramètres courant de la lumière pour
            // calculer la couleur du Vertex
            // Si désactivé : on associe simplement la couleur courante à
            // chaque vertex
            gl.glDisable(GL.GL_LIGHTING);

            // On spécifie rouge/vert/bleu pour la couleur courante.
            gl.glColor3ub((byte)255, (byte)255, (byte)255);

            // glBegin et glEnd délimitent les sommets définissant une primitive
            // ou un groupe de primitives 
            gl.glBegin(GL.GL_LINES);
                // On indique les coordonnées x et y du vertex
                gl.glVertex2i(width/2, 0);
                gl.glVertex2i(width/2, height);
                gl.glVertex2i(0, height/2);
                gl.glVertex2i(width, height/2);
            gl.glEnd();

            // GL garde la position 3D en Windows Coordinates
            // Cette position est appellée raster position. C'est utilisé pour
            // les opérations de position de pixel ou d'écriture de bitmap
            // On indique ici les coordonnées x et y de la raster position
            gl.glRasterPos2i(5, 5);
            text(gLDrawable, "Front");
            gl.glRasterPos2i(width/2+5, 5);
            text(gLDrawable, "Right");
            gl.glRasterPos2i(5, height/2+5);
            text(gLDrawable, "Top");
            gl.glRasterPos2i(width/2+5, height/2+5);
            text(gLDrawable, "Perspective");

            gl.glEnable(GL.GL_LIGHTING);

            width = (width+1)/2;
            height = (height+1)/2;

            // GL_SCISSOR_TEST enable = on jette les fragments qui sont en
            // dehors du "scissor rectangle"
            gl.glEnable(GL.GL_SCISSOR_TEST);


            // En bas à gauche
                // glViewport gère la transformation affine depuis (x,y) en
                // Normalized Device Coordinate vers les Windows Coordinates
                // On indique : le coin inférieur gauche du rectangle du
                // viewport (0 0 par défaut) ; puis la largeur/hauteur du
                // viewport 
                // NB : La première fois qu'un environnement GL est attaché à
                // une fenêtre, le viewport prend la taille de la fenêtre
                gl.glViewport(0, 0, width, height);

                // glScissor définit un rectangle nommé la Scissor box (en
                // Windows Coordinates). 
                // glEnable et glDisable avec GL_SCISSOR_TEST servent à
                // activer/désactiver le Scissor test
                // Si activé : seul les pixels à l'intérieur de la scissor box
                // peuvent être modifié par les commandes
                // NB : glScissor(0,0,1,1) = seul le pixel le plus en bas à
                // gauche change ; glScissor(0,0,0,0) = on ne peut plus rien
                // modifier
                // Si désactivé : la scissor box = toute la fenêtre
                gl.glScissor(0, 0, width, height);

                // De face
                projection(gLDrawable, width, height, false);
                
                // glRotate(theta,x,y,z) calcule une matrice qui fait faire une
                // rotation de theta degrés dans le sens trigonométrique au
                // vecteur d'origine qui part de l'origine du repère et qui
                // pointe le point (x,y,z)
                gl.glRotatef(spin_y, 1.0f, 0.0f, 0.0f);
                gl.glRotatef(spin_x, 0.0f, 1.0f, 0.0f);
           
                // glCallList(int liste) exécute séquentiellement les commandes
                // stockées dans liste, comme si on les écrivait ici.
                gl.glCallList(torus_list);

            // En bas à droite 
                gl.glViewport(width, 0, width, height);
                gl.glScissor(width, 0, width, height);
                // De droite
                projection(gLDrawable, width, height, false);
                gl.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);
                gl.glRotatef(spin_y, 1.0f, 0.0f, 0.0f);
                gl.glRotatef(spin_x, 0.0f, 1.0f, 0.0f);

                gl.glCallList(torus_list);

            // En haut à gauche
                gl.glViewport(0, height, width, height);
                gl.glScissor(0, height, width, height);
                
                // De dessus
                projection(gLDrawable, width, height, false);
                gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
                gl.glRotatef(spin_y, 1.0f, 0.0f, 0.0f);
                gl.glRotatef(spin_x, 0.0f, 1.0f, 0.0f);

                gl.glCallList(torus_list);

            // En haut à droite
                gl.glViewport(width, height, width, height);
                gl.glScissor(width, height, width, height);
                
                // Vue en perspective
                projection(gLDrawable, width, height, true);
                gl.glRotatef(30.0f, 0.0f, 1.0f, 0.0f);
                gl.glRotatef(20.0f, 1.0f, 0.0f, 0.0f);
                gl.glRotatef(spin_y, 1.0f, 0.0f, 0.0f);
                gl.glRotatef(spin_x, 0.0f, 1.0f, 0.0f);

                gl.glCallList(torus_list);

            gl.glDisable(GL.GL_SCISSOR_TEST);
            //new GLUT().glutSwapBuffers();
            processKeyboard();
        }

        /**
         * Méthode appelée en permanence (car l'appel se fait à la fin de
         * display() ) , qui lit dans keys[boolean] les touches de clavier
         * pressées par l'utilisateur et qui modifie spin_x et spin_y
         * Ces 2 int sont les angles de rotation utilisés lors du display()
         * NB: keys[] est rempli par les KeyListener de JAVA
         */
        public void processKeyboard() {

            if(keys[KeyEvent.VK_UP]) {
                // 6 car après test cela semble une bonne vitesse
                // %360 pour éviter que l'entier ne devienne trop grand
                spin_y = (spin_y - 6) % 360;
            }
            if(keys[KeyEvent.VK_DOWN]) {
                spin_y= (spin_y + 6) % 360;
            }
            if(keys[KeyEvent.VK_LEFT]) {
                spin_x= (spin_x - 6) % 360;
            }
            if(keys[KeyEvent.VK_RIGHT]) {
                spin_x= (spin_x + 6) % 360;
            }
        }






        /** 
         * Appelée quand la résolution change
         * <B>!! Pas encore implémenté en JOGL !!</B>
         * @param gLDrawable The GLDrawable object.
         * @param modeChanged Indicates if the video mode has changed.
         * @param deviceChanged Indicates if the video device has changed.
         */
        public void displayChanged(GLDrawable gLDrawable, 
                                    boolean modeChanged, 
                                    boolean deviceChanged) {
        }











        /** 
         * Appelé pendant le premier repaint après que la composante ait changée
         * de taille
         * Le client peut mettre à jour le viewport en appelant 
         * GL.glViewport(int, int, int, int)
         * 
         * NB : la composante a déjà appelé GL.glViewport(int, int, int, int)
         * (pour raison de praticité)
         * quand cette méthode est appelée, donc le client ne devrait rien avoir
         * à faire avec cette méthode
         * @param gLDrawable The GLDrawable object.
         * @param x The X Coordinate of the viewport rectangle.
         * @param y The Y coordinate of the viewport rectanble.
         * @param width The new width of the window.
         * @param height The new height of the window.
         */
        public void reshape(GLDrawable gLDrawable, 
                            int x, int y, int width, int height) {
            final GL gl = gLDrawable.getGL();
            gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        }
        
        
        void projection (GLDrawable gLDrawable, 
                         int width, int height, boolean perspective) {
            
            final GL gl = gLDrawable.getGL();
            final GLU glu = gLDrawable.getGLU();
            
            float ratio;
            if (height != 0) {
                ratio = (float)width/height;
            }
            else {
                ratio = 1.0f;
            }
            

            gl.glMatrixMode(GL.GL_PROJECTION);
            gl.glLoadIdentity();
            if (perspective) {
                
                // gluPerspective(fovy, aspect,zNear, zFar)
                // définit une matrice de projection pour la vue en perspective 
                // -fovy : angle de vue en degrée dans la direction y (field of
                //          view angle)
                // -aspect : ratio déterminant l'angle de vue dans la direction x
                //          il vaut width/height
                // -zNear : distance entre celui qui regarde et le "plan de
                //          clipping" : le plan focal
                // -zFar : distance entre celui qui regarde et le "plan lointain
                //          de clipping" : le plan objet
                glu.gluPerspective(60, ratio, 1, 256);
            }
            else {
                // multiplie la matrice courante par une matrice orthographique
                // qui semble servir pour les projections parallèles
                gl.glOrtho(-ratio, ratio, -ratio, ratio, 1, 256);
            }
            gl.glMatrixMode(GL.GL_MODELVIEW);
            gl.glLoadIdentity();
            glu.gluLookAt(0.0, 0.0, 2.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0);
        }
       
        void text(GLDrawable gLDrawable, String s) {
            
            final GL gl = gLDrawable.getGL();
            final GLU glu = gLDrawable.getGLU();

            GLUT zonk = new GLUT();

            for (int i=0 ; i<s.length() ; i++) {
                // fait le rendu d'une bitmap de character en OpenGL
                zonk.glutBitmapCharacter(gl, GLUT.BITMAP_HELVETICA_18, s.charAt(i));
            }
}
 
        
        
        
        
        
        // ON IMPLEMENTE L'INTERFACE KeyListener
        
        /** Invoked when a key has been pressed.
         * @param e The KeyEvent.
         */
        public void keyPressed(KeyEvent evt) {
            keys[evt.getKeyCode()] = true;
            if(keys[KeyEvent.VK_ESCAPE]){
                loop.stop();
                System.exit(0);
            }
        }
        
        /** Invoked when a key has been released.
         * @param e The KeyEvent.
         */
        public void keyReleased(KeyEvent evt) {
            keys[evt.getKeyCode()] = false;
        }
        
        /** Invoked when a key has been typed.
         * @param e The KeyEvent.
         */
        public void keyTyped(KeyEvent evt){}


        public class shutDownWindow extends WindowAdapter {
            public void windowClosing(WindowEvent e) {
                loop.stop();
            }
        }


        
        
    } // Fin de la classe interne








    /** Program's main entry point
     * @param args command line arguments.
     */
    public static void main(String[] args) {

        frame = new Frame("Tore sous 4 vues différentes");
        GLCanvas canvas = GLDrawableFactory.getFactory().createGLCanvas(new GLCapabilities());

        // on instancie notre classe interne
        canvas.addGLEventListener(new Renderer());
        frame.add(canvas);
        frame.setSize(640, 480);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frame.show();
        canvas.requestFocus();
    }
}
