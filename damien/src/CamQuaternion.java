    /**
     * Classe interne CamQuaternion
     * Représente les Quaternions de manière assez simple
     * Les opérations comme le conjugué ou la norme ne sont pas implémentées
     */
    strictfp class CamQuaternion {

        private float w;
        private float x;
        private float y;
        private float z;

        /**Constructeur*/
        CamQuaternion() {
            w = 1.0f;
            x = 0.0f;
            y = 0.0f;
            z = 0.0f;
        }

        /**
         * Transforme un quaternion en un quaternion représentant une
         * rotation quelconque dans l'espace
         */
        void createFromAxisAngle(float x, float y, float z, float degrees) {

            // On convertit les degrés en radians
            float angle = (degrees / 180.0f) * (float)Math.PI;

            // On calcule sinus(theta/2) une seule fois pour optimiser
            float result = (float)Math.sin(angle / 2.0f);

            // On calcule la valeur de w = cosinus(theta / 2)
            this.w = (float)Math.cos(angle / 2.0f);

            // On calcule les coordonnées x y z du quaternion
            this.x = x * result;
            this.y = y * result;
            this.z = z * result;
            normaliser();
        }

        void setValues(CamQuaternion c){
            this.w = c.w;
            this.x = c.x;
            this.y = c.y;
            this.z = c.z;
        }
        /**
         * Normalise le quaternion courant
         */
        void normaliser() {
            float norme = norme();
            // On ne normalise pas le vecteur nul
            if (norme != 0.0f) {
                w = w/norme;
                x = x/norme;
                y = y/norme;
                z = z/norme;
            }
        }

        /**
         * Renvoie la norme du quaternion courant
         */
        float norme() {
            return w*w + x*x + y*y + z*z;
        }

        /**
         * Pour créer à partir d'un quaternion une matrice qui peut être utilisée
         * par OpenGL
         */
        float[] createMatrix() {
            float[] matrix = new float[16];

            // Première COLONNE
            matrix[0] = 1.0f - 2.0f * (y*y + z*z);
            matrix[1] = 2.0f * (x*y + z*w);
            matrix[2] = 2.0f * (x*z - y*w);
            matrix[3] = 0.0f;

            // Seconde COLONNE
            matrix[4] = 2.0f * (x*y - z*w);
            matrix[5] = 1.0f - 2.0f * (x*x + z*z);
            matrix[6] = 2.0f * (z*y + x*w);
            matrix[7] = 0.0f;

            // Troisième COLONNE
            matrix[8] = 2.0f * (x*z + y*w);
            matrix[9] = 2.0f * (y*z - x*w);
            matrix[10] = 1.0f - 2.0f * (x*x + y*y);
            matrix[11] = 0.0f;

            // Quatrième COLONNE
            matrix[12] = 0.0f;
            matrix[13] = 0.0f;
            matrix[14] = 0.0f;
            matrix[15] = 1.0f;

            // matrix est une matrice 4x4 homogène qui peut être utilisée pour les
            // calculs avec les matrices OpenGL
            return matrix;
        }

        /**Pour multiplier 2 quaternions*/
        CamQuaternion multiplier(CamQuaternion q) {
            CamQuaternion resultat = new CamQuaternion();
            resultat.w = w * q.w - x * q.x - y * q.y - z * q.z;
            resultat.x = w * q.x + x * q.w + y * q.z - z * q.y;
            resultat.y = w * q.y + y * q.w + z * q.x - x * q.z;
            resultat.z = w * q.z + z * q.w + x * q.y - y * q.x;
            return resultat;
        }
        double[] getRotation() {
            double[] ret = new double[4];
            this.normaliser();
            //System.out.println("norme" + " " +this.norme());
            //this.normaliser();

            double cos_angle = this.w;
            if (Math.abs(cos_angle) > 1)
                cos_angle = 0.5;
            //double angle = Math.acos(cos_angle) * 360 / Math.PI;
            double sin_angle = Math.sqrt(1.0 - cos_angle * cos_angle);
            if (Math.abs(sin_angle) < 0.00005) {
                sin_angle = 1;
            }

            ret[0] = Math.acos(cos_angle) * 2 * 180 / Math.PI;
            ret[1] = this.x / sin_angle;
            ret[2] = this.y / sin_angle;
            ret[3] = this.z / sin_angle;

            return ret;
        }

        float[] createMatrix3(){
            float ret[] = new float[9];
            float n = norme();

            float s = 0;
            if (n>0)
                s = (float)(2.0 / n);
            float xs, ys, zs, wx, wy, wz, xx, xy, xz, yy, yz,zz;

            xs = x * s;  ys = y * s;  zs = z * s;
            wx = w * xs; wy = w * ys; wz = w * zs;
            xx = x * xs; xy = x * ys; xz = x * zs;
            yy = y * ys; yz = y * zs; zz = z * zs;
            ret[0] = 1.0f - (yy + zz);	ret[3] = xy - wz; ret[6] = xz + wy;
            ret[1] = xy + wz; ret[4] = 1.0f - (xx + zz); ret[7] = yz - wx;
            ret[2] = xz - wy; ret[5] = yz + wx; ret[8] = 1.0f - (xx + yy);

            return ret;
        }
        /*
        def Matrix3fSetRotationFromQuat4f (q1):
        # Converts the H quaternion q1 into a new equivalent 3x3 rotation matrix.
        X = 0
        Y = 1
        Z = 2
        W = 3

        NewObj = Matrix3fT ()
        n = sum (Numeric.dot (q1, q1))
        s = 0.0
        if (n > 0.0):
                s = 2.0 / n
        xs = q1 [X] * s;  ys = q1 [Y] * s;  zs = q1 [Z] * s
        wx = q1 [W] * xs; wy = q1 [W] * ys; wz = q1 [W] * zs
        xx = q1 [X] * xs; xy = q1 [X] * ys; xz = q1 [X] * zs
        yy = q1 [Y] * ys; yz = q1 [Y] * zs; zz = q1 [Z] * zs
        # This math all comes about by way of algebra, complex math, and trig identities.
        # See Lengyel pages 88-92
        NewObj [X][X] = 1.0 - (yy + zz);	NewObj [Y][X] = xy - wz; 			NewObj [Z][X] = xz + wy;
        NewObj [X][Y] =       xy + wz; 		NewObj [Y][Y] = 1.0 - (xx + zz);	NewObj [Z][Y] = yz - wx;
        NewObj [X][Z] =       xz - wy; 		NewObj [Y][Z] = yz + wx;          	NewObj [Z][Z] = 1.0 - (xx + yy)

        return NewObj
*/

    }//FIN DE LA CLASSE CamQuaternion
