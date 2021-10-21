
data "aws_vpc" "main" {
  filter {
    name   = "tag:Name"
    values = [var.vpc_name]
  }
}

data "aws_subnets" "public"{

  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.main.id]
  }

}