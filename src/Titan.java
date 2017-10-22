public class Titan {
    int id;
    String nombre, tipo;

    public Titan(int id, String nombre, String tipo) {
        this.id = id;
        this.tipo = tipo;
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return "nombre: "+nombre+", tipo: "+tipo+", ID: "+id;
    }
}
