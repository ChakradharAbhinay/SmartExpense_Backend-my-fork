packer {
  required_plugins {
    amazon = {
      version = ">= 1.2.8"
      source  = "github.com/hashicorp/amazon"
    }
  }
}

# variable "bucket_name" {
#   type    = string
#   default = ""
# }

# variable "db_host" {
#   type    = string
#   default = ""
# }

# variable "db_password" {
#   type    = string
#   default = ""
# }

variable "platform" {
  type        = string
  description = "Platform for the build (aws or gcp)"
}

# variable "ssh_private_key_file" {
#   type    = string
#   default = "C:/Users/onyeo/Downloads/new-key.pem"
# }

variable "artifact_path" {
  type        = string
  description = "Path to the artifact"
}

variable "region" {
  default = "us-east-1"
}

variable "ami_name" {
  default = "MyCustomJavaAMI01"
}

variable "instance_type" {
  default = "t2.micro"
}

variable "vpc_name" {
  default = "vpc-0e67cd00a4b047843"
}

variable "subnet_id" {
  description = "Subnet where the instance will be launched"
  type        = string
  default     = "subnet-0fe3e8a67257349e8"
}

variable "security_group_id" {
  description = "Security group for the instance"
  type        = string
  default     = "sg-07ae81bb8251458e5"
}

# Source Block (for creating the AMI)
source "amazon-ebs" "ubuntu" {
  region               = var.region
  source_ami           = "ami-0754ab7f5f3a228c7"
  instance_type        = var.instance_type
  ssh_username         = "ubuntu"
  subnet_id            = var.subnet_id
  vpc_id               = var.vpc_name
  ami_name             = var.ami_name
  ssh_keypair_name     = "new-key"
  ssh_private_key_file = var.ssh_private_key_file
  security_group_id    = var.security_group_id
  encrypt_boot         = true
}

# Build Block (using the source defined above)
build {
  sources = ["source.amazon-ebs.ubuntu"]

  # Copy the Spring Boot JAR file to the instance
  provisioner "file" {
    source      = "C:/Users/onyeo/OneDrive/Documents/GitHub/SmartExpense_Backend_Fork/smartexpense/target/smartexpense-0.0.1-SNAPSHOT.jar"
    destination = "/tmp/smartexpense-0.0.1-SNAPSHOT.jar"
  }

#   provisioner "file" {
#     source      = "./amazon-cloudwatch-agent.json"
#     destination = "/tmp/amazon-cloudwatch-agent.json"
  }

  provisioner "shell" {
    inline = [
      "echo 'Stopping unattended-upgrades service...'",
      "sudo systemctl stop unattended-upgrades || true",
      "sudo systemctl disable unattended-upgrades || true",

      "echo 'Forcing removal of apt locks if present...'",
      "sudo kill -9 $(sudo lsof -t /var/lib/dpkg/lock) 2>/dev/null || true",
      "sudo kill -9 $(sudo lsof -t /var/lib/apt/lists/lock) 2>/dev/null || true",
      "sudo rm -rf /var/lib/dpkg/lock /var/lib/dpkg/lock-frontend /var/lib/apt/lists/lock",

      "echo 'Waiting for apt lock to be released...'",
      "while sudo lsof /var/lib/dpkg/lock >/dev/null 2>&1 || sudo lsof /var/lib/apt/lists/lock >/dev/null 2>&1; do echo 'Waiting for apt lock...'; sleep 5; done",
      "echo 'Cleaning apt cache...'",

      "sudo apt-get clean",
      "sudo rm -rf /var/lib/apt/lists/*",
      "sudo mkdir -p /var/lib/apt/lists/partial",
      "sudo apt-get update -y || (echo 'Retrying...'; sleep 10; sudo apt-get update -y)",

      "echo 'Manually adding universe repository...'",
      "echo 'deb http://archive.ubuntu.com/ubuntu noble universe' | sudo tee -a /etc/apt/sources.list",
      "sudo apt-get update -y || (echo 'Retrying...'; sleep 10; sudo apt-get update -y)",

      "echo 'Installing core dependencies for Java application...'",
      "sudo apt-get install -y curl ca-certificates lsb-release unzip wget gnupg openjdk-17-jdk || (echo 'Retrying...'; sleep 10; sudo apt-get install -y curl ca-certificates lsb-release unzip wget gnupg openjdk-17-jdk)",

      "echo 'Verifying Java installation...'",
      "java -version",

      "echo 'Creating app user and directory...'",
      "sudo useradd -m -s /usr/sbin/nologin springuser",
      "sudo mkdir -p /opt/app",
      "sudo chown -R springuser:springuser /opt/app",
      "sudo chmod 755 /opt/app",

      "echo 'Moving JAR file to the correct directory...'",
      "sudo mv /tmp/smartexpense-0.0.1-SNAPSHOT.jar /opt/app/app.jar",
      "sudo chown springuser:springuser /opt/app/app.jar",
      "sudo chmod 644 /opt/app/app.jar",

      "echo 'Creating log file /opt/app/app.log...'",
      "sudo touch /opt/app/app.log",
      "sudo chown springuser:springuser /opt/app/app.log",

      # Install AWS CloudWatch Agent
    #   "echo 'Downloading AWS CloudWatch Agent...'",
    #   "curl -O https://s3.amazonaws.com/amazoncloudwatch-agent/linux/amd64/latest/AmazonCloudWatchAgent.zip",

    #   "echo 'Extracting CloudWatch Agent package...'",
    #   "sudo unzip -o AmazonCloudWatchAgent.zip -d /opt/aws/amazon-cloudwatch-agent",

    #   "echo 'Installing CloudWatch Agent...'",
    #   "sudo dpkg -i /opt/aws/amazon-cloudwatch-agent/amazon-cloudwatch-agent.deb",

    #   "echo 'Ensuring CloudWatch Agent configuration directory exists...'",
    #   "sudo mkdir -p /opt/aws/amazon-cloudwatch-agent/etc",

    #   "echo 'Moving CloudWatch Agent configuration file...'",
    #   "sudo mv /tmp/amazon-cloudwatch-agent.json /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json",
    #   "sudo chown root:root /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json",
    #   "sudo chmod 644 /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json",

    #   "echo 'Applying CloudWatch Agent configuration...'",
    #   "sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -c file:/opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json -s",

    #   "echo 'Enabling and starting CloudWatch Agent service...'",
    #   "sudo systemctl daemon-reload",
    #   "sudo systemctl enable amazon-cloudwatch-agent",
    #   "sudo systemctl restart amazon-cloudwatch-agent",

    #   "echo 'Checking CloudWatch Agent status...'",
    #   "sleep 5",
    #   "sudo systemctl status amazon-cloudwatch-agent || { echo 'CloudWatch Agent failed to start'; sudo journalctl -u amazon-cloudwatch-agent --no-pager --lines=50; exit 1; }"
    ]
  }

  # Creating and starting the systemd service for the Java app
  provisioner "shell" {
    inline = [
      "echo 'Creating systemd service for the Spring Boot app...'",
      "echo '[Unit]' | sudo tee /etc/systemd/system/springboot.service",
      "echo 'Description=Spring Boot Application' | sudo tee -a /etc/systemd/system/springboot.service",
      "echo 'After=network.target' | sudo tee -a /etc/systemd/system/springboot.service",
      "echo '[Service]' | sudo tee -a /etc/systemd/system/springboot.service",
      "echo 'User=springuser' | sudo tee -a /etc/systemd/system/springboot.service",
      "echo 'Group=springuser' | sudo tee -a /etc/systemd/system/springboot.service",
      "echo 'ExecStart=/usr/bin/java -jar /opt/app/app.jar' | sudo tee -a /etc/systemd/system/springboot.service",
      "echo 'Restart=always' | sudo tee -a /etc/systemd/system/springboot.service",
      "echo 'WorkingDirectory=/opt/app' | sudo tee -a /etc/systemd/system/springboot.service",
      "echo 'StandardOutput=file:/opt/app/app.log' | sudo tee -a /etc/systemd/system/springboot.service",
      "echo 'StandardError=file:/opt/app/app.log' | sudo tee -a /etc/systemd/system/springboot.service",
      "echo '[Install]' | sudo tee -a /etc/systemd/system/springboot.service",
      "echo 'WantedBy=multi-user.target' | sudo tee -a /etc/systemd/system/springboot.service",
      "echo 'Enabling and starting the Spring Boot service...'",
      "sudo systemctl daemon-reload",
      "sudo systemctl enable springboot",
      "sudo systemctl start springboot",

      "echo 'Checking Spring Boot service status...'",
      "sleep 5",
      "sudo systemctl status springboot || { echo 'Spring Boot service failed to start'; sudo journalctl -u springboot --no-pager --lines=50; exit 1; }"
    ]
  }
}
