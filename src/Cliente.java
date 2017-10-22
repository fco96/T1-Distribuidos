import java.io.*;
import java.net.*;
import java.util.*;

public class Cliente {

    public static class multicast implements Runnable{
        public multicast(){

        }

        @Override
        public void run() {
            try {
                byte[] buf = new byte[256];

                mcsocket = new MulticastSocket(puertoMulti);
                mcsocket.joinGroup(ipMulti);

                mcsocket.setSoTimeout(500);

                while(true){
                    try {
                        DatagramPacket paqueteRecibido = new DatagramPacket(buf, buf.length);
                        mcsocket.receive(paqueteRecibido);

                        String received = new String(paqueteRecibido.getData(), paqueteRecibido.getOffset(), paqueteRecibido.getLength());
                        System.out.println(received);
                    }catch(SocketTimeoutException e){
                        //System.out.println("Se acabó el timepo!");
                        if (refrescarMulticast==true){
                            mcsocket.close();
                            mcsocket = new MulticastSocket(puertoMulti);
                            mcsocket.joinGroup(ipMulti);
                            mcsocket.setSoTimeout(500);
                            refrescarMulticast=false;
                            System.out.println("[Cliente] Permiso concedido");
                            System.out.println("[Cliente] se ha cambiado al distrito "+nombreDistrito);
                        }
                    }
                }

            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public static class menu implements Runnable{
        public menu(){
            titanesCapturados = new ArrayList<ntitan>();
            titanesAsesinados = new ArrayList<ntitan>();
        }

        @Override
        public void run() {
            try {
                System.out.println("[Cliente] Ingresar IP servidor central");
                //ipServerCentral = InetAddress.getByName(br.readLine());
                ipServerCentral = InetAddress.getByName("127.0.0.1");

                System.out.println("[Cliente] Ingresar puerto servidor central");
                //puertoServerCentral = Integer.parseInt(br.readLine());
                puertoServerCentral = 8080;

                System.out.println("[Cliente] Ingresar nombre del distrito a investigar");
                nombreDistrito = br.readLine();
                //nombreDistrito = "Trost";

                //Se crea el socket para comunicarme con la central
                socketCentral = new DatagramSocket();

                // Se envia petición al servidor central para ver si me puedo conectar al distrito
                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(nombreDistrito.getBytes(), nombreDistrito.getBytes().length, ipServerCentral, puertoServerCentral);
                socketCentral.send(packet);

                // Esperar la respuesta
                packet = new DatagramPacket(buf, buf.length);
                socketCentral.receive(packet);

                // Procesar la respuesta
                String received = new String(packet.getData(), packet.getOffset(), packet.getLength());
                received = received.trim();
                String[] a = received.split("-");

                if (received.equals("NOEXISTE")){
                    System.out.println("[Cliente] El distrito "+nombreDistrito+" no existe.");
                }
                else if (a[0].trim().equals("NO")==false){
                    System.out.println("Permiso concedido");
                    ipMulti = InetAddress.getByName(a[2]);
                    ipDistrito = InetAddress.getByName(a[0]);
                    puertoMulti = Integer.valueOf(a[3].trim());
                    puertoDistrito = Integer.valueOf(a[1].trim());

                    Thread myThread2 = new Thread(new multicast());
                    myThread2.start();
                    socketDistrito = new DatagramSocket();
                    while (true){
                        System.out.println("[Cliente] Consola");
                        System.out.println("[Cliente] (1) Listar titanes");
                        System.out.println("[Cliente] (2) Cambiar distrito");
                        System.out.println("[Cliente] (3) Capturar titan");
                        System.out.println("[Cliente] (4) Asesinar titan");
                        System.out.println("[Cliente] (5) Lista titanes capturados");
                        System.out.println("[Cliente] (6) Lista titanes Asesinados");
                        System.out.println("[Cliente] (7) Cerrar cliente");
                        String opcion = br.readLine();
                        //LISTAR LOS TITANES
                        if (opcion.equals("1")){
                            //Enviar paquete
                            String mensaje = "1";
                            buf = mensaje.getBytes();
                            DatagramPacket paqueteDistritoEnviado = new DatagramPacket(buf, buf.length, ipDistrito, puertoDistrito);
                            socketDistrito.send(paqueteDistritoEnviado);

                            Boolean deboSeguir = true;

                            //Ciclo while que va a pedirle titanes(al distrito) hasta que envie que no quedan más
                            while(deboSeguir){
                                //Esperar la respuesta
                                buf = new byte[256];
                                DatagramPacket paqueteDistritoRecibido = new DatagramPacket(buf, buf.length);
                                socketDistrito.receive(paqueteDistritoRecibido);

                                //Procesar la respuesta
                                String respuesta = new String(paqueteDistritoRecibido.getData(), paqueteDistritoRecibido.getOffset(), paqueteDistritoRecibido.getLength());
                                a = respuesta.split("-");

                                if (a[0].trim().equals("COMPLETADO")){
                                    deboSeguir=false;
                                }
                                else if (a[0].trim().equals("NOHAY")){
                                    deboSeguir=false;
                                    System.out.println("[Cliente] Actualmente no hay titanes en "+nombreDistrito+".");
                                }
                                else{
                                    System.out.println("[Cliente] En el distrito de: "+nombreDistrito+" se encuentra el titan "+a[0]+", de tipo "+a[1]+" y de ID "+a[2]);
                                }
                            }

                        }
                        //CAMBIARSE DE DISTRITO
                        else if (opcion.equals("2")){
                            System.out.println("[Cliente] Ingrese el nuevo distrito al cual se quiere cambiar");
                            String consulta = br.readLine().trim();

                            //Se procede a consultar si me puedo cambiar de distrito
                            packet = new DatagramPacket(consulta.getBytes(), consulta.getBytes().length, ipServerCentral, puertoServerCentral);
                            socketCentral.send(packet);

                            buf = new byte[256];
                            DatagramPacket paqueteServidorRecibido = new DatagramPacket(buf, buf.length);
                            socketCentral.receive(paqueteServidorRecibido);

                            //Se procesa la respuesta
                            received = new String(paqueteServidorRecibido.getData(), paqueteServidorRecibido.getOffset(), paqueteServidorRecibido.getLength());
                            a = received.split("-");
                            if (a[0].trim().equals("NO")==false) {
                                nombreDistrito = consulta.trim();
                                ipMulti = InetAddress.getByName(a[2]);
                                ipDistrito = InetAddress.getByName(a[0]);
                                puertoMulti = Integer.valueOf(a[3].trim());
                                puertoDistrito = Integer.valueOf(a[1].trim());
                                refrescarMulticast = true;

                            }
                            else{
                                System.out.println("[Cliente] Cambio denegado, usted sigue en "+nombreDistrito);
                            }


                        }
                        //CAPTURAR UN TITAN
                        else if (opcion.equals("3")){
                            System.out.println("[Cliente] Ingrese el id del titan a capturar");
                            String id = br.readLine();

                            //Enviar paquete
                            String mensaje = "3-"+id;
                            buf = mensaje.getBytes();
                            DatagramPacket paqueteDistritoEnviado = new DatagramPacket(buf, buf.length, ipDistrito, puertoDistrito);
                            socketDistrito.send(paqueteDistritoEnviado);

                            //Esperar la respuesta
                            buf = new byte[256];
                            DatagramPacket paqueteDistritoRecibido = new DatagramPacket(buf, buf.length);
                            socketDistrito.receive(paqueteDistritoRecibido);

                            //Procesar la respuesta
                            String respuesta = new String(paqueteDistritoRecibido.getData(), paqueteDistritoRecibido.getOffset(), paqueteDistritoRecibido.getLength());
                            a = respuesta.split("-");

                            if (respuesta.trim().equals("NO")){
                                System.out.println("[Cliente] ID de titan no encontrado en "+nombreDistrito);
                            }
                            else if (a[0].trim().equals("FALLO")){
                                System.out.println("[Cliente] Ha fallado la captura de "+a[1]+", pues es un titan excentrico.");
                            }
                            //Caso en el que si funcionó la captura
                            else {
                                System.out.println("[Cliente] El titan "+a[0]+" ha sido capturado exitosamente!");
                                int n = Integer.valueOf(a[2].trim());
                                titanesCapturados.add(new ntitan(n, a[0], a[1], nombreDistrito));

                            }
                        }
                        //ASESINAR UN TITAN
                        else if (opcion.equals("4")){
                            System.out.println("[Cliente] Ingrese el id del titan a asesinar");
                            String id = br.readLine();

                            //Enviar paquete
                            String mensaje = "4-"+id;
                            buf = mensaje.getBytes();
                            DatagramPacket paqueteDistritoEnviado = new DatagramPacket(buf, buf.length, ipDistrito, puertoDistrito);
                            socketDistrito.send(paqueteDistritoEnviado);

                            //Esperar la respuesta
                            buf = new byte[256];
                            DatagramPacket paqueteDistritoRecibido = new DatagramPacket(buf, buf.length);
                            socketDistrito.receive(paqueteDistritoRecibido);

                            //Procesar la respuesta
                            String respuesta = new String(paqueteDistritoRecibido.getData(), paqueteDistritoRecibido.getOffset(), paqueteDistritoRecibido.getLength());
                            a = respuesta.split("-");

                            if (respuesta.trim().equals("NO")){
                                System.out.println("[Cliente] ID de titan no encontrado en "+nombreDistrito);
                            }
                            else if (a[0].trim().equals("FALLO")){
                                System.out.println("[Cliente] Ha fallado el asesinato de "+a[1]+", pues es un titan cambiante.");
                            }
                            //Caso en el que si funcionó el asesinato
                            else {
                                System.out.println("[Cliente] El titan "+a[0]+" ha sido asesinado exitosamente!");
                                int n = Integer.valueOf(a[2].trim());
                                titanesAsesinados.add(new ntitan(n, a[0], a[1], nombreDistrito));

                            }
                        }
                        //MOSTRAR TITANES CAPTURADOS
                        else if (opcion.equals("5")){
                            if (titanesCapturados.size()==0){
                                System.out.println("[Cliente] Usted no tiene ningún titan capturado");
                            }
                            else {
                                System.out.println("[Cliente] Los titanes capturados son");
                                for (ntitan t : titanesCapturados) {
                                    System.out.println(t);
                                }
                            }
                        }
                        //MOSTRAR TITANES ASESINADOS
                        else if (opcion.equals("6")){
                            if (titanesAsesinados.size()==0){
                                System.out.println("[Cliente] Usted no ha asesinado ningún titan");
                            }
                            else {
                                System.out.println("[Cliente] Los titanes que usted ha asesinado son: ");
                                for (ntitan t : titanesAsesinados) {
                                    System.out.println(t);
                                }
                            }
                        }
                        //TERMINAR EJECUCION
                        else if (opcion.equals("7")){
                            //Se procede a informar que terminará la ejecución
                            String mensaje = "SALIR";
                            packet = new DatagramPacket(mensaje.getBytes(), mensaje.getBytes().length, ipServerCentral, puertoServerCentral);
                            socketCentral.send(packet);

                            myThread2.stop();
                            return;



                        }
                        else{
                            System.out.println("Opción '"+opcion+"' inválida. ");
                        }

                    }
                }
                else if (received.equals("NO")){
                    System.out.println("[Cliente] No se ha autorizado el acceso a "+nombreDistrito);
                }


            } catch (IOException e){
                e.printStackTrace();
            }


        }
    }

    static String nombreDistrito;
    static MulticastSocket mcsocket;
    static InetAddress ipMulti;
    static InetAddress ipServerCentral, ipDistrito;
    static int puertoServerCentral, puertoMulti, puertoDistrito;
    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    static DatagramSocket socketCentral = null;
    static DatagramSocket socketDistrito = null;
    static List <ntitan> titanesCapturados = null;
    static List <ntitan> titanesAsesinados = null;
    static Boolean refrescarMulticast = false;

    static class ntitan extends Titan{
        String origen;

        public ntitan(int id, String nombre, String tipo, String origen) {
            super(id, nombre, tipo);
            this.origen = origen;
        }

        @Override
        public String toString() {
            return "ID: "+id+", nombre: "+nombre+", tipo: "+tipo+", ciudad de origen: "+origen;
        }
    }

    public static void main(String[] args){
        Thread myThread1 = new Thread(new menu());
        //Thread myThread2 = new Thread(new multicast());
        myThread1.start();
        //myThread2.start();
    }
}
