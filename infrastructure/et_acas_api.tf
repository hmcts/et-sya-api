module "api-et-acas-mgmt-product" {
  source = "git@github.com:hmcts/cnp-module-api-mgmt-product?ref=master"

  api_mgmt_name                 = local.api_mgmt_name
  api_mgmt_rg                   = local.api_mgmt_rg
  name                          = var.et_acas_product_name
  product_access_control_groups = ["developers"]
  approval_required             = "false"
  subscription_required         = "true"
  providers = {
    azurerm = azurerm.aks-cftapps
  }
}

module "et-acas-mgmt-api" {
  source = "git@github.com:hmcts/cnp-module-api-mgmt-api?ref=master"

  api_mgmt_name = local.api_mgmt_name
  api_mgmt_rg   = local.api_mgmt_rg
  revision      = "1-2"
  service_url   = local.et_sya_api_url
  product_id    = module.api-et-acas-mgmt-product.product_id
  name          = join("-", [var.et_acas_product_name, "api"])
  display_name  = "ET SYA ACAS Api"
  path          = "et-sya-api"
  protocols     = ["http", "https"]
  swagger_url   = var.acas_swagger_url

  providers = {
    azurerm = azurerm.aks-cftapps
  }
}

data "template_file" "et_acas_policy_template" {
  template = file(join("", [path.module, "/templates/api-policy.xml"]))

  vars = {
    s2s_client_id     = data.azurerm_key_vault_secret.s2s_client_id.value
    s2s_client_secret = data.azurerm_key_vault_secret.et_sya_api_s2s_key.value
    s2s_base_url      = local.s2sUrl
  }
}

module "mdl-et-acas-policy" {
  source        = "git@github.com:hmcts/cnp-module-api-mgmt-api-policy?ref=master"
  api_mgmt_name = local.api_mgmt_name
  api_mgmt_rg   = local.api_mgmt_rg

  api_name               = module.et-acas-mgmt-api.name
  api_policy_xml_content = data.template_file.et_acas_policy_template.rendered

  providers = {
    azurerm = azurerm.aks-cftapps
  }
}

resource "azurerm_api_management_subscription" "et_acas_subscription" {
  api_management_name = local.api_mgmt_name
  resource_group_name = local.api_mgmt_rg
  user_id             = azurerm_api_management_user.et_api_management_user.id
  product_id          = module.api-et-acas-mgmt-product.id
  display_name        = "ET SYA ACAS Subscription"
  state               = "active"
  provider            = azurerm.aks-cftapps
}

resource "azurerm_key_vault_secret" "et_acas_subscription_key" {
  key_vault_id = module.key-vault.key_vault_id
  name         = "etacas-subscription-key"
  value        = azurerm_api_management_subscription.et_acas_subscription.primary_key
}
