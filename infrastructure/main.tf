provider "azurerm" {
  features {}
}

locals {
  tagEnv = var.env == "aat" ? "staging" : var.env == "perftest" ? "testing" : var.env == "prod" ? "production" : var.env
  tags = merge(var.common_tags,
    tomap({
      "environment"  = local.tagEnv,
      "managedBy"    = var.team_name,
      "Team Contact" = var.team_contact,
      "application"  = "employment-tribunals",
      "businessArea" = "CFT",
      "builtFrom"    = "et-sya-api"
    })
  )

  api_mgmt_suffix = var.apim_suffix == "" ? var.env : var.apim_suffix
  api_mgmt_name   = "cft-api-mgmt-${local.api_mgmt_suffix}"
  api_mgmt_rg     = join("-", ["cft", var.env, "network-rg"])

  et_sya_api_url = join("", ["http://et-sya-api-", var.env, ".service.core-compute-", var.env, ".internal"])
  s2sUrl         = join("", ["http://rpe-service-auth-provider-", var.env, ".service.core-compute-", var.env, ".internal"])
}

resource "azurerm_resource_group" "rg" {
  name     = "${var.product}-${var.component}-${var.env}"
  location = var.location
  tags     = local.tags
}

data "azurerm_user_assigned_identity" "et-identity" {
  name                = "${var.product}-${var.env}-mi"
  resource_group_name = "managed-identities-${var.env}-rg"
}

data "azurerm_key_vault" "s2s_vault" {
  name                = "s2s-${var.env}"
  resource_group_name = "rpe-service-auth-provider-${var.env}"
}

data "azurerm_key_vault_secret" "et_sya_api_s2s_key" {
  name         = "microservicekey-et-sya-api"
  key_vault_id = data.azurerm_key_vault.s2s_vault.id
}

module "key-vault" {
  source                      = "git@github.com:hmcts/cnp-module-key-vault?ref=master"
  name                        = "${var.product}-${var.component}-${var.env}"
  product                     = var.product
  env                         = var.env
  tenant_id                   = var.tenant_id
  object_id                   = var.jenkins_AAD_objectId
  resource_group_name         = azurerm_resource_group.rg.name
  product_group_name          = "DTS Employment Tribunals"
  common_tags                 = local.tags
  managed_identity_object_ids = [data.azurerm_user_assigned_identity.et-identity.principal_id]
}


resource "azurerm_key_vault_secret" "et_sya_api_s2s_secret" {
  name         = "et-sya-api-s2s-secret"
  value        = data.azurerm_key_vault_secret.et_sya_api_s2s_key.value
  key_vault_id = module.key-vault.key_vault_id
}

data "azurerm_key_vault_secret" "s2s_client_id" {
  key_vault_id = module.key-vault.key_vault_id
  name         = "et-sya-api-s2s-client-id"
}

data "azurerm_key_vault" "et-msg-handler-vault" {
  name                = "et-msg-handler-${var.env}"
  resource_group_name = "et-msg-handler-${var.env}"
}

data "azurerm_key_vault_secret" "et-api-caseworker-username" {
  name         = "caseworker-user-name"
  key_vault_id = data.azurerm_key_vault.et-msg-handler-vault.id
}

data "azurerm_key_vault_secret" "et-api-caseworker-password" {
  name         = "caseworker-password"
  key_vault_id = data.azurerm_key_vault.et-msg-handler-vault.id
}

resource "azurerm_key_vault_secret" "et-caseworker-user-name" {
  key_vault_id = module.key-vault.key_vault_id
  name         = "et-api-caseworker-user-name"
  value        = data.azurerm_key_vault_secret.et-api-caseworker-username.value
}

resource "azurerm_key_vault_secret" "et-caseworker-password" {
  key_vault_id = module.key-vault.key_vault_id
  name         = "et-api-caseworker-password"
  value        = data.azurerm_key_vault_secret.et-api-caseworker-password.value
}

provider "azurerm" {
  alias           = "aks-cftapps"
  subscription_id = var.aks_subscription_id
  features {}
}
