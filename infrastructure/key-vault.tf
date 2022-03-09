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
