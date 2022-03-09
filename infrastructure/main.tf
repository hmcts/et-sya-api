provider "azurerm" {
  features {}
}

locals {
  tags = merge(var.common_tags,
    map(
      "environment", var.env,
      "managedBy", var.team_name,
      "Team Contact", var.team_contact,
    )
  )
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

data "azurerm_key_vault_secret" "et_sya_s2s_key" {
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


resource "azurerm_key_vault_secret" "et_sya_s2s_secret" {
  name         = "et-sya-s2s-secret"
  value        = data.azurerm_key_vault_secret.et_sya_s2s_key.value
  key_vault_id = module.key-vault.key_vault_id
}
