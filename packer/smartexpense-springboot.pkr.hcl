variable "artifact" {
  type = string
}

source "amazon-ebs" "springboot" {
  region           = "us-east-1"
  source_ami       = "ami-0c55b159cbfafe1f0" # Amazon Linux 2
  instance_type    = "t2.micro"
  ssh_username     = "ec2-user"
  ami_name         = "springboot-app-{{timestamp}}"
}

build {
  sources = ["source.amazon-ebs.springboot"]

  provisioner "file" {
    source      = var.artifact
    destination = "/home/ec2-user/smartexpense-0.0.1-SNAPSHOT.jar"
  }

  provisioner "shell" {
    inline = [
      "sudo yum install -y java-17-openjdk",
      "nohup java -jar /home/ec2-user/smartexpense-0.0.1-SNAPSHOT.jar > app.log 2>&1 &"
    ]
  }
}

