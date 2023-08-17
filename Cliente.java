import socket
import threading
class Cliente:
        def _init_(self, host, puerto):
        self.host = host
        self.puerto = puerto

        def iniciar(self):
        self.socket_cliente = socket.socket(socket.AF_INET, socket.SOCK_STREAM)  // Crea un socket TCP
        self.socket_cliente.connect((self.host, self.puerto))  // Se conecta al servidor del broker
        self.hilo_escucha = threading.Thread(target=self.escuchar_mensajes)  // Crea un hilo para escuchar mensajes
        self.hilo_escucha.start()  // Inicia el hilo de escucha

        def escuchar_mensajes(self):
        while True:
        datos = self.socket_cliente.recv(1024).decode()  // Recibe datos del servidor
        if datos:
        partes = datos.split(",")  // Divide los datos en partes usando coma como separador
        comando = partes[0]  // El primer elemento es el comando

        if comando == "MENSAJE":
        topico = partes[1]  // El segundo elemento es el tópico del mensaje
        mensaje = partes[2]  // El tercer elemento es el mensaje en sí
        print(f"Mensaje recibido en el tópico '{topico}': {mensaje}")
        self.enviar_ack()  // Envía una confirmación de entrega (ACK)

        def suscribir(self, topico):
        self.socket_cliente.send(f"SUSCRIBIR,{topico}".encode())  // Envía un comando de suscripción al servidor

        def publicar(self, topico, mensaje):
        self.socket_cliente.send(f"PUBLICAR,{topico},{mensaje}".encode())  // Envía un comando de publicación al servidor

        def enviar_ack(self):
        self.socket_cliente.send("ACK".encode())  // Envía una confirmación de entrega (ACK) al servidor

        if _name_ == "_main_":
        cliente = Cliente("127.0.0.1", 8888)  // Crea una instancia del cliente con la dirección del servidor y el puerto
        cliente.iniciar()  // Inicia la conexión y el hilo de escucha

        while True:
        accion = input("Ingresa 'S' para suscribirte o 'P' para publicar: ").upper()
        if accion == "S":
        topico = input("Ingresa el tópico al que deseas suscribirte: ")
        cliente.suscribir(topico)  // Permite al usuario suscribirse a un tópico
        elif accion == "P":
        topico = input("Ingresa el tópico en el que deseas publicar: ")
        mensaje = input("Ingresa el mensaje que deseas publicar: ")
        cliente.publicar(topico, mensaje)  // Permite al usuario publicar un mensaje en un tópico
        else:
        print("Acción no válida. Ingresa 'S' para suscribirte o 'P' para publicar.")  // Mensaje de acción inválida