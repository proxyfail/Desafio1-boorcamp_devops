pipeline {
    agent any
    parameters {
        string(name: 'LOGIN', defaultValue: '', description: 'Login del usuario')
        string(name: 'NOMBRE', defaultValue: '', description: 'Nombre del usuario')
        string(name: 'APELLIDO', defaultValue: '', description: 'Apellido del usuario')
        string(name: 'DEPARTAMENTO', defaultValue: '', description: 'Departamento del usuario')
    }
    stages {
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
        stage('Mostrar Contraseña Temporal') {
            steps {
                script {
                    def login = params.LOGIN
                    def password = readFile("${login}_password.txt").trim()
                    echo "La contraseña temporal para el usuario ${login} es: ${password}"
                }
            }
        }
    }
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
}