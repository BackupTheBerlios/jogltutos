import net.java.games.jogl.*;
import net.java.games.jogl.util.*;
import java.awt.event.*;
import java.awt.Robot;
import java.awt.*;
import javax.swing.ImageIcon;


public class QCam implements KeyListener, MouseListener,
        MouseMotionListener {

    Robot robot;
    Cursor emptyCursor;
    public QCam() {
        try {
            robot = new Robot();

        } catch (AWTException ex) {
            ex.printStackTrace();
            }
            px = py = 0;
            pz = 1;
            ex = ey = ez = 0;
    }

    public void mousePressed(MouseEvent e) {
        /*robot.mouseMove(e.getComponent().getWidth() / 2,
                        e.getComponent().getHeight() / 2
                );*/

        if (emptyCursor == null) {
            ImageIcon emptyIcon = new ImageIcon(new byte[0]);
            emptyCursor = e.getComponent().getToolkit().createCustomCursor(
                    emptyIcon.getImage(), new Point(0, 0), "emptyCursor");
        }
        e.getComponent().setCursor(emptyCursor);
    }

    public void mouseReleased(MouseEvent e) {
    }

    double px, py, pz, ex, ey, ez;
    private int win_x;
    private int win_y;
    private int win_w2;
    private int win_h2;
    private int real_w;
    private int real_h;
    private double sx, sy, sz;

    public void setPerspective(GLDrawable gld) {
        final GLU glu = gld.getGLU();
        final GL gl = gld.getGL();
        GLUT glut = new GLUT();
        //gl.glPushMatrix();





        //glu.gluLookAt(ex, ey, ez, px,py,pz, 0, 1, 0);
        glu.gluLookAt(0, 0, 0, px,py,pz, 0, 1, 0);
        gl.glPushMatrix();
        gl.glTranslated(px, py, pz);
        gl.glColor3d(1,1,1);
        glut.glutSolidSphere(glu,.005,20,20);
        gl.glPopMatrix();
gl.glTranslated(ex, 0, ez);

        //System.out.println(px +" " +py + " " +pz);
        //glu.gluPerspective(45, 4/3, 0, 10);
        //double nx, ny, nz, n;
        //n = Math.sqrt(x*x+y*y+z*z);
        /*nx = x/n;
                 ny = y/n;
                 nz = z/n;*/


        //System.out.println("R " + ex + " " + ez);

    }

    public void keyPressed(KeyEvent e) {
        //On accelère
        double cons = 3;
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            ex -= px/cons;
            ez -= pz/cons;

        }

        //On ralentit
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            ex += px/cons;
            ez += pz/cons;
        }

    }



public void centerMouse(Component c){
    win_x = 0;
    win_y = 0;
    win_w2 = c.getWidth() / 2;
    win_h2 = c.getHeight() / 2;

    while (c != null) {
        if (c instanceof Container) {
            Insets insets = ((Container) c).getInsets();
            win_x += insets.left;
            win_y += insets.top;
        }
        win_x += c.getX();
        win_y += c.getY();
        c = c.getParent();
    }
    real_w = 2 * (win_x + win_w2);
    real_h = 2 * (win_y + win_h2);
  garde = 1;
    robot.mouseMove(win_x + win_w2, win_y + win_h2);
    garde = 0;
}
int garde = 0;
public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void mouseDragged(MouseEvent e) {

    }
    int xx = 1;
    int inv = 1;
    public void mouseMoved(MouseEvent e) {
        int mx, my;
        /*if (xx == 1){
            mx = my = 0;
            xx = 0;
        }
         else{*/
             mx = e.getX() - real_w/2;
         my = e.getY() - real_h/2;
         //}

        double c = 1;
        sx += inv*mx*c;
        sy += my*c;

        if (Math.abs(sx) >= real_w/2){
            inv = inv == 1 ? -1 : 1;
            //sx = sx<0?-real_w/2+0.5:real_w/2-0.5;
            sx += inv * mx * c;
            //sx += inv * mx * c;
            //sx += inv * mx * c;

            //sy += inv * mx * c;

            /*sx = sx<0?-real_w/2:real_w/2;

             sx += inv * mx * c;*/
            //centerMouse(e.getComponent());
            //sx = sy = 0;
        }
        if (Math.abs(sy) > real_h/2)
             sy = sy<0?-real_h/2:real_h/2;
        //System.out.println(sx +" "+sy);
        /*px += Math.abs(mx * c);
        py += Math.abs(my * c);*/
    px = -sx;
    py = -sy;
    double val = real_w*real_w/4 - px*px - py*py;
    if (val > 0)
        sz = inv * Math.sqrt(real_w*real_w/4 - px*px - py*py);
    else sz = 0;
    pz = sz;
        double n = Math.sqrt(px*px+py*py+pz*pz);
        if (n == 0) n = 1;
        px /= n;
        py /= n;
        pz /= n;
        //System.out.println(px + " " + py + " " + pz);
        System.out.println(sx + " " + sy + " " );


        centerMouse(e.getComponent());
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }


}
