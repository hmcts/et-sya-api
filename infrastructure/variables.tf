variable "product" {
  default = "et"
}

variable "component" {
  default = "sya-api"
}

variable "location" {
  default = "UK South"
}

variable "env" {}

variable "subscription" {}

variable "tenant_id" {}

variable "jenkins_AAD_objectId" {
  description = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "deployment_namespace" {
  default = ""
}

variable "common_tags" {
  type = map(string)
}

variable "team_name" {
  description = "Team name"
  default     = "Employment Tribunals"
}

variable "team_contact" {
  description = "Team contact"
  default     = "#et-devs"
}

variable "apim_suffix" {
  default = ""
}

variable "et_acas_product_name" {
  type    = string
  default = "et-acas"
}

variable "aks_subscription_id" {
}

variable "acas_swagger_url" {
  default = "https://raw.githubusercontent.com/hmcts/reform-api-docs/master/docs/specs/et-acas-api.json"
}

variable "soft_delete_retention_days" {
  default = 9
}
