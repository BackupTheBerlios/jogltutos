//Pour les fen�tres
import java.awt.*;
import java.awt.event.*;

//Pour le jogl
import net.java.games.jogl.*;
import net.java.games.jogl.util.GLUT;

/** 
 * Portage de multiview.c de Nate Robins en jogl
 * On affiche un tore en fil de fer sous 4 vues diff�rentes
 *
 * L'inter�t est purement p�dagogique
 */
public class Tore {

    // Frame d�finie ici et pas dans le main car la classe interne doit y
    // acc�der pour pouvoir conna�tre la taille de la fen�tre
    static Frame frame; 


    /**
     * Note sur <b>GLEventListener</b> : utilis� pour rappeler nos composantes
     * graphiques si on doit les red�ssiner ou modifier leur apparence
     *
     * L'interface d�finit 4 m�thodes :
     * -<i>init (GLDrawable drawable)</i> : 
     *      appel�e � l'initialisation de l'OpenGL (et donc utile pour les 
     *      cas o� on dessine une seule fois)
     *
     * -<i>display (GLDrawable drawable)</i> : 
     *      pour que la composante se dessine elle-m�me
     *
     * -<i>reshape (GLDrawable drawable, 
                    int i, int x, int width, int height)</i> : 
     *      signale que la composante a chang� de taille ou d'emplacement
     *
     * -<i>displayChanged (GLDrawable drawable, 
     *                     boolean modeChanged, 
     *                     boolean deviceChanged)</i> : 
     *      pour signaler que le display mode (r�solution || nb de couleurs) a
     *      �t� chang� (par l'utilisateur sans doute), ou que la fen�tre a �t�
     *      d�plac�e sur un autre �cran <b> Non support� pour le moment</b>
     *
     * NB : Au d�marrage, on appelle init(), reshape(), puis display()
     *      Apr�s : reshape() si changement de taille de fen�tre
     *              display() tout le temps
     */
    static class Renderer implements GLEventListener, KeyListener {

        // POUR DEPLACER LE TORE AU CLAVIER
        // Tableau stockant les touches du clavier press�es.
        boolean keys[] = new boolean[256];
        // Si on appuie sur les fl�ches gauche ou droite on modifiera spin_x
        float spin_x = 0.0f;
        // Si on appuie sur les fl�ches haut ou bas on modifiera spin_y
        float spin_y = 0.0f;
        // FIN CLAVIER

        //int  maxtokens = 0;
        int torus_list = 0;
        
        // Un Animator peut �tre attach� � un GLDrawable object pour g�rer sa
        // m�thode display() dans une boucle. Pour plus d'efficacit�, l'Animator
        // fait que le thread de rendu (rendering) du GLDrawable object devient
        // son propre thread (donc on ne peut pas combiner
        // cette m�thode avec des "repaint" manuels de la surface par ex)
        Animator loop;


        /** 
         * Pour initialiser le rendering OpenGL
         * <B>C'est quoi un GLDrawable object ?</B>
         * En gros c'est l'"interface" basique qui nous relie � OpenGL
         * (Apparement c'est un directement un bon gros dump de gl.h)
         * Le mieux c'est <i>de ne pas le voir comme un objet, mais juste comme une
         * sorte de handle pour faire des appels de m�thodes</i>
         * En cas de portage depuis OpenGL, les fonctions commen�ant par gl et
         * les constantes commen�ant par GL sont accessibles gr�ce � cette
         * instance de la classe GL
         *
         * @param gLDrawable The GLDrawable object.
         */
        public void init(GLDrawable gLDrawable) {

            final GL gl = gLDrawable.getGL();
            final GLU glu = gLDrawable.getGLU();

            float[] light_pos = { 1.0f, 1.0f, 1.0f, 0.0f };

            //GL_LIGHT_MODEL_TWO_SIDE : pour notre tore en fil de fer on �claire
            //la face normalement visible ("� l'ext�rieur du grand rayon") ET 
            // la face "interne" ("� l'ext�rieur du petit rayon")
            gl.glLightModeli(GL.GL_LIGHT_MODEL_TWO_SIDE, GL.GL_TRUE);

            // glLightfv(nb de lumi�re, type de source, 
            // On met une lumi�re : GL_LIGHT0 (0 indique la premi�re lumi�re)
            // De type GL_POSITION : selon la position la lumi�re prend certains
            // param�tres sp�cifiques (elle est directionnelle apparement)
            gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, light_pos);
            gl.glEnable(GL.GL_LIGHTING);
            gl.glEnable(GL.GL_LIGHT0);
            gl.glEnable(GL.GL_DEPTH_TEST);
            
            // Si activ� : on ne voit pas les faces cach�es
            // Ici d�sactiv� car en fil de fer c'est int�r�ssant de voir
            // l'int�rieur du tore
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

            // Une liste de commandes stock�es, qu'on peut appeler par un
            // glCallList()
            // glGenLists prend en param�tre le nombre de display liste vide
            // qu'on veut g�n�rer
            torus_list = gl.glGenLists(1);
                gl.glNewList(torus_list, GL.GL_COMPILE);
                    
                    // glMaterialf assigne des valeurs aux param�tres du
                    // "material" : il y a 2 jeux de param�tres, le premier est
                    // utilis� pour les points, lignes, bitmap, polygones ;
                    // l'autre sert pour les faces arri�res si on utilise le
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
            

            //Cr�er et lancer un Animator est simple :
            //  on le construit avec un GLDisplayable argument
            //  puis on le d�marre avec start()
            loop = new Animator(gLDrawable);
            loop.start();




            
        }




        /**
         * Pour qu'une composante puisse se dessiner
         * Appel�e par la composante pour d�buter le rendu OpenGL par le client
         */
        public void display(GLDrawable gLDrawable) {

            /**
             * NB : Le GL object est obtenu du GLDrawable object pass� dans init()
             * Tous les GLEventListener fournissent cet objet, et le <i>JOGL User's
             * Guide</i> conseille de toujours prendre une r�f�rence r�cente vers le
             * GL object depuis le GLDrawable object � chaque appel, plut�t que de
             * le stocker pour le r�utiliser (pb de thread dans AWT, mieux vaut
             * �viter un appel OpenGL depuis le mauvais thread)
             */
            final GL gl = gLDrawable.getGL();

            /**
             * De m�me, quand on doit faire un appel � glu.h, on prend le GLU
             * object depuis le GLDrawable object
             */
            final GLU glu = gLDrawable.getGLU();

            
            // On r�cup�re la taille de la fen�tre pour que le viewport
            // occupe 100% de la fen�tre
            int width = (int)frame.getSize().getWidth();
            int height = (int)frame.getSize().getHeight();


            // On d�finit le viewport : zone dans laquelle on travaille
            // NB : En OpenGL l'origine (0,0) est en bas � gauche et pas en haut �
            // gauche comme sous AWT
            gl.glViewport(0, 0, width, height);

            // On sp�cifie quelle pile de matrice sera la cible pour les
            // op�rations qui vont suivre. 3 valeurs possibles :
            //      GL_MODELVIEW, GL_PROJECTION, et GL_TEXTURE.
            gl.glMatrixMode(GL.GL_PROJECTION);

            // On remplace la matrice en sommet de pile par l'identit�
            gl.glLoadIdentity();

            // D�finit une matrice de projection orthographique 2D
            glu.gluOrtho2D(0, width, 0, height);

            // On s'adresse maintenant � la pile de matrice GL_MODELVIEW
            gl.glMatrixMode(GL.GL_MODELVIEW);

            // On remplace la matrice en sommet de pile par l'identit�
            gl.glLoadIdentity();
            
            // On indique les valeurs rouge/vert/bleu/alpha � utiliser quand le
            // buffer des couleurs est vid� (0 0 0 0 par d�faut) 
            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

            // On d�sactive GL_LIGHTING.
            // Si activ� : on utilise les param�tres courant de la lumi�re pour
            // calculer la couleur du Vertex
            // Si d�sactiv� : on associe simplement la couleur courante �
            // chaque vertex
            gl.glDisable(GL.GL_LIGHTING);

            // On sp�cifie rouge/vert/bleu pour la couleur courante.
            gl.glColor3ub((byte)255, (byte)255, (byte)255);

            // glBegin et glEnd d�limitent les sommets d�finissant une primitive
            // ou un groupe de primitives 
            gl.glBegin(GL.GL_LINES);
                // On indique les coordonn�es x et y du vertex
                gl.glVertex2i(width/2, 0);
                gl.glVertex2i(width/2, height);
                gl.glVertex2i(0, height/2);
                gl.glVertex2i(width, height/2);
            gl.glEnd();

            // GL garde la position 3D en Windows Coordinates
            // Cette position est appell�e raster position. C'est utilis� pour
            // les op�rations de position de pixel ou d'�criture de bitmap
            // On indique ici les coordonn�es x et y de la raster position
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


            // En bas � gauche
                // glViewport g�re la transformation affine depuis (x,y) en
                // Normalized Device Coordinate vers les Windows Coordinates
                // On indique : le coin inf�rieur gauche du rectangle du
                // viewport (0 0 par d�faut) ; puis la largeur/hauteur du
                // viewport 
                // NB : La premi�re fois qu'un environnement GL est attach� �
                // une fen�tre, le viewport prend la taille de la fen�tre
                gl.glViewport(0, 0, width, height);

                // glScissor d�finit un rectangle nomm� la Scissor box (en
                // Windows Coordinates). 
                // glEnable et glDisable avec GL_SCISSOR_TEST servent �
                // activer/d�sactiver le Scissor test
                // Si activ� : seul les pixels � l'int�rieur de la scissor box
                // peuvent �tre modifi� par les commandes
                // NB : glScissor(0,0,1,1) = seul le pixel le plus en bas �
                // gauche change ; glScissor(0,0,0,0) = on ne peut plus rien
                // modifier
                // Si d�sactiv� : la scissor box = toute la fen�tre
                gl.glScissor(0, 0, width, height);

                // De face
                projection(gLDrawable, width, height, false);
                
                // glRotate(theta,x,y,z) calcule une matrice qui fait faire une
                // rotation de theta degr�s dans le sens trigonom�trique au
                // vecteur d'origine qui part de l'origine du rep�re et qui
                // pointe le point (x,y,z)
                gl.glRotatef(spin_y, 1.0f, 0.0f, 0.0f);
                gl.glRotatef(spin_x, 0.0f, 1.0f, 0.0f);
           
                // glCallList(int liste) ex�cute s�quentiellement les commandes
                // stock�es dans liste, comme si on les �crivait ici.
                gl.glCallList(torus_list);

            // En bas � droite 
                gl.glViewport(width, 0, width, height);
                gl.glScissor(width, 0, width, height);
                // De droite
                projection(gLDrawable, width, height, false);
                gl.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);
                gl.glRotatef(spin_y, 1.0f, 0.0f, 0.0f);
                gl.glRotatef(spin_x, 0.0f, 1.0f, 0.0f);

                gl.glCallList(torus_list);

            // En haut � gauche
                gl.glViewport(0, height, width, height);
                gl.glScissor(0, height, width, height);
                
                // De dessus
                projection(gLDrawable, width, height, false);
                gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
                gl.glRotatef(spin_y, 1.0f, 0.0f, 0.0f);
                gl.glRotatef(spin_x, 0.0f, 1.0f, 0.0f);

                gl.glCallList(torus_list);

            // En haut � droite
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
         * M�thode appel�e en permanence (car l'appel se fait � la fin de
         * display() ) , qui lit dans keys[boolean] les touches de clavier
         * press�es par l'utilisateur et qui modifie spin_x et spin_y
         * Ces 2 int sont les angles de rotation utilis�s lors du display()
         * NB: keys[] est rempli par les KeyListener de JAVA
         */
        public void processKeyboard() {

            if(keys[KeyEvent.VK_UP]) {
                // 6 car apr�s test cela semble une bonne vitesse
                // %360 pour �viter que l'entier ne devienne trop grand
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
         * Appel�e quand la r�solution change
         * <B>!! Pas encore impl�ment� en JOGL !!</B>
         * @param gLDrawable The GLDrawable object.
         * @param modeChanged Indicates if the video mode has changed.
         * @param deviceChanged Indicates if the video device has changed.
         */
        public void displayChanged(GLDrawable gLDrawable, 
                                    boolean modeChanged, 
                                    boolean deviceChanged) {
        }











        /** 
         * Appel� pendant le premier repaint apr�s que la composante ait chang�e
         * de taille
         * Le client peut mettre � jour le viewport en appelant 
         * GL.glViewport(int, int, int, int)
         * 
         * NB : la composante a d�j� appel� GL.glViewport(int, int, int, int)
         * (pour raison de praticit�)
         * quand cette m�thode est appel�e, donc le client ne devrait rien avoir
         * � faire avec cette m�thode
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
                // d�finit une matrice de projection pour la vue en perspective 
                // -fovy : angle de vue en degr�e dans la direction y (field of
                //          view angle)
                // -aspect : ratio d�terminant l'angle de vue dans la direction x
                //          il vaut width/height
                // -zNear : distance entre celui qui regarde et le "plan de
                //          clipping" : le plan focal
                // -zFar : distance entre celui qui regarde et le "plan lointain
                //          de clipping" : le plan objet
                glu.gluPerspective(60, ratio, 1, 256);
            }
            else {
                // multiplie la matrice courante par une matrice orthographique
                // qui semble servir pour les projections parall�les
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

        frame = new Frame("Tore sous 4 vues diff�rentes");
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
