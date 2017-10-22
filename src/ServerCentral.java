import java.io.*;
import java.net.*;
import java.util.*;


public class ServerCentral {

    public static class distribuidor implements Runnable{
        //Socket que será usado para que la los distritos vean el asunto del numero de id
        DatagramSocket socketDistribuidor;
        int id_titan = 1;
        public distribuidor(){

        }

        @Override
        public void run() {
            try {
                //Se crea el scoket en un puerto arbitrario
                socketDistribuidor = new DatagramSocket(8081);

                //Se procede a atender las peticiones
                while (true){
                    //Buffer para recibir la info
                    byte[] buf = new byte[256];

                    // Se procede a recibir una petición
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socketDistribuidor.receive(packet);

                    String idTexto = Integer.toString((id_titan));
                    packet = new DatagramPacket(idTexto.getBytes(), idTexto.getBytes().length, packet.getAddress(), packet.getPort());
                    socketDistribuidor.send(packet);
                    id_titan+=1;


                }

            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    static class distrito{
        String nombre;
        String ipMulti;
        int puertoMulti;
        String ipPeticiones;
        int puertoPeticiones;

        public distrito(String nombre, String ipMulti, int puertoMulti, String ipPeticiones, int puertoPeticiones) {
            this.nombre = nombre;
            this.ipMulti = ipMulti;
            this.puertoMulti = puertoMulti;
            this.ipPeticiones = ipPeticiones;
            this.puertoPeticiones = puertoPeticiones;
        }
        @Override
        public String toString() {
            return "distrito{" +
                    "nombre='" + nombre + '\'' +
                    ", ipMulti=" + ipMulti +
                    ", puertoMulti=" + puertoMulti +
                    ", ipPeticiones=" + ipPeticiones +
                    ", puertoPeticiones=" + puertoPeticiones +
                    '}';
        }

    }

    public static class menu implements Runnable{
        public menu(){
            distritos = new ArrayList<distrito>();
            clientes = new ArrayList<ncliente>();
            try {
                socket = new DatagramSocket(8080);
            }catch (SocketException e){
                e.printStackTrace();
            }
        }

        @Override
        public void run() {

            try {
                while (true) {
                    System.out.println("Seleccione una opción:");
                    System.out.println("1) Agregar distritos trost");
                    System.out.println("2) Santiago");
                    System.out.println("3) Empezar a servir");
                    String opcion = br.readLine();
                    if ("1".equals(opcion)) {
                        System.out.println("[Servidor central] Nombre distrito: ");
                        //String nombre = br.readLine();
                        String nombre = "Trost";

                        System.out.println("[Servidor central] IP Multicast: ");
                        //String ipMulti = br.readLine();
                        String ipMulti = "224.1.1.1";

                        System.out.println("[Servidor central] Puerto Multicast: ");
                        //int puertoMulti = Integer.parseInt(br.readLine());
                        int puertoMulti = 4545;

                        System.out.println("[Servidor central] IP Peticiones: ");
                        //String ipPeticiones = br.readLine();
                        String ipPeticiones = "127.0.0.1";

                        System.out.println("[Servidor central] Puerto Peticiones: ");
                        //int puertoPeticiones = Integer.parseInt(br.readLine());
                        int puertoPeticiones = 3434;


                        distritos.add(new distrito(nombre, ipMulti, puertoMulti, ipPeticiones, puertoPeticiones));
                        //System.out.println("Los datos ingresados son" + nombre + " " + ipMulti + ":" + puertoMulti + " " + ipPeticiones + ":" + puertoPeticiones);

                    }
                    else if ("2".equals(opcion)) {
                        System.out.println("[Servidor central] Nombre distrito: ");
                        //String nombre = br.readLine();
                        String nombre = "Santiago";

                        System.out.println("[Servidor central] IP Multicast: ");
                        //String ipMulti = br.readLine();
                        String ipMulti = "224.1.1.1";

                        System.out.println("[Servidor central] Puerto Multicast: ");
                        //int puertoMulti = Integer.parseInt(br.readLine());
                        int puertoMulti = 4546;

                        System.out.println("[Servidor central] IP Peticiones: ");
                        //String ipPeticiones = br.readLine();
                        String ipPeticiones = "127.0.0.1";

                        System.out.println("[Servidor central] Puerto Peticiones: ");
                        //int puertoPeticiones = Integer.parseInt(br.readLine());
                        int puertoPeticiones = 3435;


                        distritos.add(new distrito(nombre, ipMulti, puertoMulti, ipPeticiones, puertoPeticiones));
                        //System.out.println("Los datos ingresados son" + nombre + " " + ipMulti + ":" + puertoMulti + " " + ipPeticiones + ":" + puertoPeticiones);

                    }
                    else if ("3".equals(opcion)) {
                        /*System.out.println("SE PROCEDE A MOSTRAR LOS DISTRITOS");
                        for (distrito d : distritos) {
                            System.out.println(d);
                        }*/
                        //Se corre el thread que genera avisos periódicos de los clientes conectados
                        Thread myThread3 = new Thread(new aviso());
                        myThread3.start();

                        System.out.println("[Servidor central] Se procede a aceptar peticiones");
                        Thread ThreadDistribuidor = new Thread(new distribuidor());
                        ThreadDistribuidor.start();
                        while (true) {
                            try {
                                byte[] buf = new byte[256];

                                // Recibir una petición
                                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                                socket.receive(packet);

                                // Generar la respuesta
                                String distritoSolicitado = new String(packet.getData(), packet.getOffset(), packet.getLength());
                                distritoSolicitado = distritoSolicitado.trim();
                                distritoSolicitado.replaceAll("\\P{Print}", "");

                                if (distritoSolicitado.equals("SALIR")) {
                                    for(int i = 0; i< clientes.size(); i++){
                                        if ((clientes.get(i).dirreccion.equals(packet.getAddress())) && (clientes.get(i).puerto == packet.getPort())){
                                            clientes.remove(i);
                                            break;
                                        }
                                    }
                                }
                                else{
                                    System.out.println("[Servidor central] Dar autorización a " + packet.getAddress() + " para " + distritoSolicitado);
                                    System.out.println("1) SI");
                                    System.out.println("2) NO");
                                    String autorizado = new String(br.readLine());
                                    autorizado.replaceAll("\\P{Print}", "");
                                    Boolean encontrado = false;
                                    if ("1".equals(autorizado)) {
                                        for (distrito d : distritos) {
                                            if (d.nombre.equals(distritoSolicitado)) {
                                                String mensaje = d.ipPeticiones + "-" + d.puertoPeticiones + "-" + d.ipMulti + "-" + d.puertoMulti;
                                                System.out.println("[Servidor central] Nombre del distrito: " + d.nombre + " ip Multicast: " + d.ipMulti + ":" + d.puertoMulti + " y ip Peticiones: " + d.ipPeticiones + ":" + d.puertoPeticiones);
                                                buf = mensaje.getBytes();
                                                encontrado = true;
                                                //LISTA DE CLIENTES
                                                Boolean estaba = false;
                                                for (ncliente c : clientes) {
                                                    if ((c.dirreccion.equals(packet.getAddress())) && (c.puerto==packet.getPort())) {
                                                        c.distrito = distritoSolicitado;
                                                        estaba = true;
                                                    }
                                                }
                                                if (estaba == false) {
                                                    clientes.add(new ncliente(packet.getAddress(), packet.getPort(), distritoSolicitado));
                                                }
                                                break;
                                            }

                                        }
                                        if (encontrado == false) {
                                            System.out.println("[Servidor central] Distrito no encontrado");
                                            buf = "NOEXISTE".getBytes();
                                        }
                                    } else if ("2".equals(autorizado)) {
                                        buf = "NO".getBytes();
                                    }
                                    // Responder
                                    packet = new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort());
                                    socket.send(packet);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }


            } catch (IOException e){
                e.printStackTrace();
            }


        }
    }

    public static class aviso implements Runnable{
        byte[] buf;
        DatagramPacket paqueteEnviadoMulticast;
        @Override
        public void run() {
            try {
                while (true) {
                    System.out.println("[Servidor Centra] *Inicio de aviso periodico de clientes*");

                    if (clientes.size() == 0) {
                        System.out.println("[Servidor central] Actualmente no hay clientes conectados.");
                    }

                    for (ncliente n : clientes) {
                        System.out.println("[Servidor] IP: " + n.dirreccion + ":" + n.puerto + ", distrito: "+n.distrito);
                    }
                    Thread.sleep(60 * 1000);
                }
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }


    static DatagramSocket socket = null;
    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    static List<distrito> distritos = null;
    static List<ncliente> clientes = null;

    static class ncliente{
        InetAddress dirreccion;
        int puerto;
        String distrito;

        public ncliente(InetAddress dirreccion, int puerto, String distrito) {
            this.dirreccion = dirreccion;
            this.puerto = puerto;
            this.distrito = distrito;
        }
    }

    public static void main(String[] args){
        Thread myThread1 = new Thread(new menu());
        //Thread myThread2 = new Thread(new multicast());
        myThread1.start();
        //myThread2.start();
    }
}
