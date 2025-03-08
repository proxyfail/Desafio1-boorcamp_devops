# Desafio1-boorcamp_devops

En el diagrama "altaDeUsuario.excalidraw", graficamos la creacion de un usuario de Linux con los datos: id unico, nombre y apellido, departamento. Estos datos una vez creados son resguardados para posteriormente ser enviados al usuario final.

El archivo "altaDeUsuarios.groovy" es un script de pipeline de Jenkins escrito en Groovy. Su propósito es crear un nuevo usuario en un sistema Linux, generar una contraseña temporal para el usuario, mostrar la contraseña temporal y enviarla por correo electrónico. A continuación, se explica el funcionamiento del archivo:

1. **Definición del pipeline**:
    ```groovy
    pipeline {
        agent any
    ```
El pipeline se ejecuta en cualquier agente disponible.

2. **Parámetros de entrada**:
    ```groovy
    parameters {
        string(name: 'LOGIN', defaultValue: '', description: 'Login del usuario')
        string(name: 'NOMBRE', defaultValue: '', description: 'Nombre del usuario')
        string(name: 'APELLIDO', defaultValue: '', description: 'Apellido del usuario')
        string(name: 'DEPARTAMENTO', defaultValue: '', description: 'Departamento del usuario')
    }
    ```
Se definen cuatro parámetros de entrada: `LOGIN`, `NOMBRE`, `APELLIDO` y `DEPARTAMENTO`, que son necesarios para crear el usuario.

3. **Etapas del pipeline**:
    - **Crear Usuario**:
        ```groovy
        stage('Crear Usuario') {
            steps {
                script {
                    def login = params.LOGIN
                    def nombre = params.NOMBRE
                    def apellido = params.APELLIDO
                    def departamento = params.DEPARTAMENTO
                    def password = UUID.randomUUID().toString().substring(0, 8)
                    
                    // Crear el usuario en Linux
                    sh """
                    sudo useradd -m -c "${nombre} ${apellido}, ${departamento}" -s /bin/bash ${login}
                    echo "${login}:${password}" | sudo chpasswd
                    sudo chage -d 0 ${login}
                    """
                    
                    // Guardar la contraseña temporal
                    writeFile file: "${login}_password.txt", text: password
                }
            }
        }
        ```
        En esta etapa, se crean variables para los parámetros de entrada y se genera una contraseña temporal. Luego, se ejecuta un comando shell para crear el usuario en Linux, asignarle la contraseña temporal y forzar el cambio de contraseña en el próximo inicio de sesión. Finalmente, se guarda la contraseña temporal en un archivo.

    - **Mostrar Contraseña Temporal**:
        ```groovy
        stage('Mostrar Contraseña Temporal') {
            steps {
                script {
                    def login = params.LOGIN
                    def password = readFile("${login}_password.txt").trim()
                    echo "La contraseña temporal para el usuario ${login} es: ${password}"
                }
            }
        }
        ```
        En esta etapa, se lee la contraseña temporal desde el archivo y se muestra en la consola de Jenkins.

4. **Post-actions**:
    ```groovy
    post {
        always {
            script {
                def login = params.LOGIN
                // Enviar la contraseña temporal por correo electrónico
                emailext subject: "Contraseña temporal para ${login}",
                         body: "La contraseña temporal para el usuario ${login} es: ${readFile("${login}_password.txt").trim()}",
                         to: 'email@example.com'
            }
        }
    }
    ```
Después de que el pipeline se ejecuta, siempre se envía un correo electrónico con la contraseña temporal al usuario especificado.

Este pipeline automatiza el proceso de creación de usuarios en un sistema Linux, asegurando que cada usuario reciba una contraseña temporal de manera segura.

En la carpeta de "Evidencias" se guardaron las capturas de pantalla que evidencian el correcto funcionamiento del pipeline, ademas se encuentra tambien un log de la ejecucion del pipeline.

Por ultimo, dentro del archivo "log.txt", se encuentra la evidencia de la salida por consola del pipeline corriendo por Jenkins. Adjunto ademas capturas de pantalla de la ejecucion.