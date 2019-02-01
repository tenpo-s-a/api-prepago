provider "kong" {
  kong_admin_uri = "https://kong.${var.cluster-environment}.${var.cluster-domain}/admin"
}

resource "kong_service" "service" {
	name     	= "api-prepaid"
	protocol 	= "http"
	host     	= "api-prepaid.api-prepaid.svc.cluster.local"
	port     	= 80
	path     	= "/"
}

resource "kong_route" "route" {
	protocols 	    = [ "http", "https" ]
	methods 	    = [ "GET", "POST" ]
	hosts 		    = [ "kong.${var.cluster-environment}.${var.cluster-domain}" ]
	paths 		    = [ "/prepaid" ]
	strip_path 	    = true
	preserve_host 	= false
	regex_priority 	= 1
	service_id 	    = "${kong_service.service.id}"
}
