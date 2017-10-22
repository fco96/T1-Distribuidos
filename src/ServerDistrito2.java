import java.io.*;
import java.net.*;
import java.util.*;

public class ServerDistrito2 {

    public static class servidor implements Runnable {
        public servidor(){
            try {
                br = new BufferedReader(new InputStreamReader(System.in));

                System.out.println("[Distrito] Nombre distrito: ");
                //nombre = br.readLine();
                nombre = "Santiago";

                System.out.println("[Distrito "+nombre+"] IP Servidor Central: ");
                //ipServerCentral = InetAddress.getByName(br.readLine());
                ipServerCentral = InetAddress.getByName("127.0.0.1");

                System.out.println("[Distrito "+nombre+"] IP Multicast: ");
                //ipMulti = InetAddress.getByName(br.readLine());
                ipMulti = InetAddress.getByName("224.1.1.1");

                System.out.println("[Distrito "+nombre+"] Puerto Multicast: ");
                //puertoMulti = Integer.parseInt(br.readLine());
                puertoMulti = 4546;

                System.out.println("[Distrito "+nombre+"] IP Peticiones: ");
                //ipPeticiones = InetAddress.getByName(br.readLine());
                ipPeticiones = InetAddress.getByName("127.0.0.1");

                System.out.println("[Distrito "+nombre+"] Puerto Peticiones: ");
                //puertoPeticiones = Integer.parseInt(br.readLine());
                puertoPeticiones = 3435;

                //Generación socket para atender

                //socket = new DatagramSocket(puertoPeticiones, ipPeticiones);
                socket = new DatagramSocket(puertoPeticiones);

                //Generación socked multicast
                mcsocket = new DatagramSocket();

                //Mando a ejecutar el menu
                Thread myThread2 = new Thread(new menu());
                myThread2.start();

                Thread myThread3 = new Thread(new aviso());
                myThread3.start();

            } catch (IOException e){
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                //PROCEDO A ATENDER PETICIONES RECIBIDAS EN EL SOCKET DE PETICIONES

                while (true) {
                    //Se recibe el paquete
                    byte[] buf = new byte[256];
                    DatagramPacket paqueteClienteRecibido = new DatagramPacket(buf, buf.length);
                    socket.receive(paqueteClienteRecibido);

                    String received = new String(paqueteClienteRecibido.getData(), paqueteClienteRecibido.getOffset(), paqueteClienteRecibido.getLength());
                    String[] opcion = received.trim().split("-");

                    //Paquete que le será enviado multiples veces a cada cliente
                    DatagramPacket paqueteClienteEnviado;
                    if (opcion[0].equals("1")) {

                        //Caso en el que no hay titanes
                        if (titanes.size() == 0) {
                            //Procedo a generar la respuesta
                            String mensaje = "NOHAY";
                            buf = mensaje.getBytes();
                            paqueteClienteEnviado = new DatagramPacket(buf, buf.length, paqueteClienteRecibido.getAddress(), paqueteClienteRecibido.getPort());
                            socket.send(paqueteClienteEnviado);
                        } else { //Caso en el que si hay titanes

                            for (Titan t : titanes) {
                                String mensaje = t.nombre + "-" + t.tipo + "-" + t.id;
                                buf = mensaje.getBytes();
                                paqueteClienteEnviado = new DatagramPacket(buf, buf.length, paqueteClienteRecibido.getAddress(), paqueteClienteRecibido.getPort());
                                socket.send(paqueteClienteEnviado);

                            }

                            String mensaje = "COMPLETADO";
                            buf = mensaje.getBytes();
                            paqueteClienteEnviado = new DatagramPacket(buf, buf.length, paqueteClienteRecibido.getAddress(), paqueteClienteRecibido.getPort());
                            socket.send(paqueteClienteEnviado);


                        }


                    }
                    //Caso en el que quieren capturar un titan
                    else if (opcion[0].equals("3")) {
                        boolean encontrado = false;

                        for (int i = 0; i < titanes.size(); i++) {
                            //Encontre el titan que buscaba
                            if (titanes.get(i).id == Integer.valueOf(opcion[1].trim())) {
                                String mensaje, mensajeGlobal;
                                if (titanes.get(i).tipo.equals("excentrico")) {
                                    mensaje = "FALLO-" + titanes.get(i).nombre;
                                } else {
                                    mensaje = titanes.get(i).nombre + "-" + titanes.get(i).tipo + "-" + titanes.get(i).id;
                                    mensajeGlobal = "[Clientes de " + nombre + "] Atención el titan " + titanes.get(i).nombre + " ha sido capturado";
                                    mensajeGlobal = mensajeGlobal.trim();
                                    titanes.remove(i);
                                    //Se envía el anuncio global de que fue capturado
                                    DatagramPacket mcPaquete = new DatagramPacket(mensajeGlobal.getBytes(), mensajeGlobal.getBytes().length, ipMulti, puertoMulti);
                                    mcsocket.send(mcPaquete);
                                }
                                //Se envia la respuesta de los datos del titan
                                buf = mensaje.getBytes();
                                paqueteClienteEnviado = new DatagramPacket(buf, buf.length, paqueteClienteRecibido.getAddress(), paqueteClienteRecibido.getPort());
                                socket.send(paqueteClienteEnviado);
                                encontrado = true;
                                break;
                            }
                        }
                        if (encontrado == false) {
                            String mensaje = "NO";
                            buf = mensaje.getBytes();
                            paqueteClienteEnviado = new DatagramPacket(buf, buf.length, paqueteClienteRecibido.getAddress(), paqueteClienteRecibido.getPort());
                            socket.send(paqueteClienteEnviado);
                        }

                    }
                    //CASO EN QUE QUIEREN ASESINAR UN TITAN
                    else if (opcion[0].equals("4")) {
                        boolean encontrado = false;

                        for (int i = 0; i < titanes.size(); i++) {
                            //Encontre el titan que buscaba
                            if (titanes.get(i).id == Integer.valueOf(opcion[1].trim())) {
                                String mensaje, mensajeGlobal;
                                if (titanes.get(i).tipo.equals("cambiante")) {
                                    mensaje = "FALLO-" + titanes.get(i).nombre;
                                } else {
                                    mensaje = titanes.get(i).nombre + "-" + titanes.get(i).tipo + "-" + titanes.get(i).id;
                                    mensajeGlobal = "[Clientes de " + nombre + "] Atención el titan " + titanes.get(i).nombre + " ha sido asesinado";
                                    mensajeGlobal = mensajeGlobal.trim();
                                    titanes.remove(i);
                                    //Se envía el anuncio global de que fue asesinado
                                    DatagramPacket mcPaquete = new DatagramPacket(mensajeGlobal.getBytes(), mensajeGlobal.getBytes().length, ipMulti, puertoMulti);
                                    mcsocket.send(mcPaquete);
                                }
                                //Se envia la respuesta de los datos del titan
                                buf = mensaje.getBytes();
                                paqueteClienteEnviado = new DatagramPacket(buf, buf.length, paqueteClienteRecibido.getAddress(), paqueteClienteRecibido.getPort());
                                socket.send(paqueteClienteEnviado);
                                encontrado = true;
                                break;
                            }
                        }
                        if (encontrado == false) {
                            String mensaje = "NO";
                            buf = mensaje.getBytes();
                            paqueteClienteEnviado = new DatagramPacket(buf, buf.length, paqueteClienteRecibido.getAddress(), paqueteClienteRecibido.getPort());
                            socket.send(paqueteClienteEnviado);
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

                    String mensaje = "[Clientes de " + nombre + "] *Inicio de aviso periódico de titanes*";
                    buf = mensaje.getBytes();
                    paqueteEnviadoMulticast = new DatagramPacket(buf, buf.length, ipMulti, puertoMulti);
                    mcsocket.send(paqueteEnviadoMulticast);

                    if (titanes.size() == 0) {
                        mensaje = "[Clientes de " + nombre + "] Por ahora no hay titanes";
                        buf = mensaje.getBytes();
                        paqueteEnviadoMulticast = new DatagramPacket(buf, buf.length, ipMulti, puertoMulti);
                        mcsocket.send(paqueteEnviadoMulticast);
                    } else {
                        for (Titan t : titanes) {
                            mensaje = "[Clientes de " + nombre + "] Esta el titan" + t;

                            buf = mensaje.getBytes();
                            paqueteEnviadoMulticast = new DatagramPacket(buf, buf.length, ipMulti, puertoMulti);
                            mcsocket.send(paqueteEnviadoMulticast);

                        }
                    }
                    Thread.sleep(60 * 1000);
                }
            } catch (IOException e){
                e.printStackTrace();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    public static class menu implements Runnable {
        @Override
        public void run() {
            try {
                //Socket destinado a a preguntar el id del titan
                DatagramSocket socketSincronizador = new DatagramSocket();
                byte[] buf;
                titanes = new ArrayList<Titan>();
                while (true){
                    System.out.println("[Distrito " + nombre + "]Ingrese lo que desea hacer");
                    System.out.println("1) Publicar titan");
                    String opcion = br.readLine();
                    if (opcion.equals("1")) {
                        System.out.println("[Distrito " + nombre + " ] Introducir nombre");
                        String name = br.readLine();
                        System.out.println("[Distrito " + nombre + " ] Introducir tipo");
                        System.out.println("1.- Normal");
                        System.out.println("2.- Excentrico");
                        System.out.println("3.- Cambiante");
                        int type = Integer.parseInt(br.readLine());
                        //System.out.println("Se ingreso al titan " + name + " de tipo " + type);

                        //Procedo a pedir el id de titan disponible
                        //Enviar el paquete
                        DatagramPacket packet = new DatagramPacket(" ".getBytes(), " ".getBytes().length, ipServerCentral, 8081);
                        socketSincronizador.send(packet);

                        // Recibir el paquete
                        buf = new byte[256];
                        DatagramPacket paqueteRecibido = new DatagramPacket( buf, buf.length);
                        socketSincronizador.receive(paqueteRecibido);

                        String idTexto = new String(paqueteRecibido.getData(), paqueteRecibido.getOffset(), paqueteRecibido.getLength());
                        int id = Integer.valueOf(idTexto.trim());


                        String tipo;
                        if (type == 1){
                            tipo = "normal";
                        }
                        else if (type == 2){
                            tipo = "excentrico";
                        }
                        else{
                            tipo = "cambiante";
                        }
                        titanes.add(new Titan(id, name, tipo));
                        System.out.println("[Distrito " + nombre + "] Se ha publicado el titán: "+name);
                        System.out.println("******************");
                        System.out.println(" ID: "+id);
                        System.out.println(" Nombre: "+name);
                        System.out.println(" Tipo: "+tipo);
                        System.out.println("******************");

                        String mensaje = "[Clientes de "+nombre+"] Aparece un nuevo titan! "+name+", tipo "+tipo+", ID "+id+".";

                        buf = mensaje.getBytes();
                        packet = new DatagramPacket(buf, buf.length, ipMulti, puertoMulti);
                        mcsocket.send(packet);

                    }
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    static BufferedReader br = null;
    static String nombre;
    static InetAddress ipMulti, ipPeticiones, ipServerCentral;
    static int puertoMulti, puertoPeticiones;
    static DatagramSocket socket = null;
    static DatagramSocket mcsocket = null;
    static List <Titan> titanes = null;

    public static void main(String[] args){
        Thread myThread1 = new Thread(new servidor());

        myThread1.start();

    }

}
