import socket
import threading
class ServidorBroker:
        def _init_(self, host, puerto):
        self.host = host
        self.puerto = puerto
        self.topicos = {}  // Diccionario para almacenar los clientes suscritos a cada tópico
        self.bloqueo = threading.Lock()  // Bloqueo para gestionar el acceso concurrente a los datos

        def iniciar(self):
        self.socket_servidor = socket.socket(socket.AF_INET, socket.SOCK_STREAM)  // Crea un socket TCP
        self.socket_servidor.bind((self.host, self.puerto))  // Asocia el socket al host y puerto especificados
        self.socket_servidor.listen(5)  // Comienza a escuchar por conexiones entrantes
        print(f"Servidor Broker escuchando en {self.host}:{self.puerto}")

        while True:
        socket_cliente, direccion_cliente = self.socket_servidor.accept()  // Acepta una conexión entrante
        hilo_cliente = threading.Thread(target=self.manipular_cliente, args=(socket_cliente,))
        hilo_cliente.start()  // Inicia un hilo para manejar al cliente

        def manipular_cliente(self, socket_cliente):
        with socket_cliente:
        direccion_cliente = socket_cliente.getpeername()
        print(f"Nuevo cliente conectado: {direccion_cliente}")

        while True:
        try:
        datos = socket_cliente.recv(1024).decode()  // Recibe datos del cliente
        if not datos:
        self.eliminar_cliente(socket_cliente)  // Desconexión del cliente
        break

        partes = datos.split(",")  // Divide los datos en partes usando coma como separador
        comando = partes[0]  // El primer elemento es el comando

        if comando == "SUSCRIBIR":
        topico = partes[1]  // El segundo elemento es el tópico al que suscribirse
        self.suscribir(socket_cliente, topico)  // Suscripción a un tópico

        elif comando == "PUBLICAR":
        topico = partes[1]  // El segundo elemento es el tópico al que publicar
        mensaje = partes[2]  // El tercer elemento es el mensaje a publicar
        self.publicar(topico, mensaje)  // Publicación de un mensaje en un tópico

        except Exception as e:
        print(f"Error al manejar al cliente {direccion_cliente}: {e}")
        self.eliminar_cliente(socket_cliente)
        break

        def suscribir(self, socket_cliente, topico):
        with self.bloqueo:
        if topico not in self.topicos:
        self.topicos[topico] = []
        self.topicos[topico].append(socket_cliente)
        print(f"Cliente suscrito al tópico {topico}")

        def publicar(self, topico, mensaje):
        with self.bloqueo:
        if topico in self.topicos:
        for socket_cliente in self.topicos[topico]:
        try:
        socket_cliente.send(f"MENSAJE,{topico},{mensaje}".encode())
        print(f"Enviado mensaje al cliente suscrito al tópico {topico}")
        self.esperar_ack(socket_cliente)  // Espera una confirmación de entrega (ACK)
        except:
        print(f"Error al enviar mensaje al cliente suscrito al tópico {topico}")
        else:
        print(f"No hay clientes suscritos al tópico {topico}")

        def esperar_ack(self, socket_cliente):
        try:
        ack = socket_cliente.recv(1024).decode()
        if ack == "ACK":
        print("Confirmación de entrega del mensaje recibida")
        except:
        pass

        def eliminar_cliente(self, socket_cliente):
        with self.bloqueo:
        for topico, clientes in self.topicos.items():
        if socket_cliente in clientes:
        clientes.remove(socket_cliente)
        print(f"Cliente se ha desuscrito del tópico {topico}")
        print(f"Cliente desconectado: {socket_cliente.getpeername()}")

        if _name_ == "_main_":
        broker = ServidorBroker("127.0.0.1", 8888)  // Crea una instancia del servidor con dirección y puerto
        broker.iniciar()  // Inicia el servidor para aceptar conexiones entrantes