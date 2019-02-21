provider "consul" {
  address    = "consul.tools.multicajadigital.cloud"
  datacenter = "eastus2"
}

# Access a key in Consul
data "consul_keys" "app" {
  datacenter = "eastus2"

  # Llave de conexion a las maquinas
  key {
    name    = "kong_api_key"
    path    = "tools/kong/${var.cluster_environment}/apikey"
  }
}
