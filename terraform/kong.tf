provider "kong" {
  kong_admin_uri = "${var.kong_admin_uri}"
  kong_api_key = "${data.consul_keys.app.var.kong_api_key}"
}

resource "kong_service" "service" {
  name     = "prepaid-api"
  protocol = "http"
  host     = "api-prepaid.api-prepaid.svc.cluster.local"
  path     = "/api-prepaid-1.0/1.0/prepaid/processor/notification"
}

resource "kong_route" "route" {
  protocols  = ["http", "https"]
  paths      = ["/prepaid/processor/notification"]
  service_id = "${kong_service.service.id}",
  regex_priority = 1
}

resource "kong_consumer" "tecnocom_consumer" {
  username  = "tecnocom-consumer"
  custom_id = "tecnocom"
}

resource "kong_consumer" "prepago_consumer" {
  username  = "prepago-consumer"
  custom_id = "prepago"
}

resource "kong_consumer_plugin_config" "tecnocom_consumer" {
  consumer_id = "${kong_consumer.tecnocom_consumer.id}"
  plugin_name = "key-auth"

  config_json = <<EOT
        {
	    "key": "d7cV620VRNwTntMVA7wiaAYIFh2nO6Is"
	}
EOT
}

resource "kong_consumer_plugin_config" "prepago_consumer" {
  consumer_id = "${kong_consumer.prepago_consumer.id}"
  plugin_name = "key-auth"

  config_json = <<EOT
        {
	    "key": "zK9ymQQd4fMw6oidkpG8h4bo6z9twawQ"
	}
EOT
}

resource "kong_plugin" "key_auth" {
	name                = "key-auth"
	service_id = "${kong_service.service.id}"

  config_json = <<EOT
        {
	    "key_names": "x-api-key,api-key"
	}
EOT
}
