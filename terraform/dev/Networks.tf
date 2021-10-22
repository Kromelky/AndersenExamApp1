
data "aws_vpc" "main" {
  filter {
    name   = "tag:Name"
    values = ["AndVPC"]
  }
}

data "aws_route_table" "rt" {
  filter {
    name   = "tag:Name"
    values = ["AndMainRT"]
  }
}

data "aws_availability_zones" "available" {
  state = "available"
}

#Adding subnet for each host
resource "aws_subnet" "public" {
  count = var.instance_count
  cidr_block = cidrsubnet(var.vpc_cidr, 8, count.index+2)
  vpc_id = data.aws_vpc.main.id
  availability_zone = data.aws_availability_zones.available.names[(count.index + 1) % length(data.aws_availability_zones.available.names)]
  tags = {
    Name = cidrsubnet(var.vpc_cidr, 8, count.index+2)
  }
}

resource "aws_route_table_association" "public" {
  count = var.instance_count
  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = data.aws_route_table.rt.id
}
