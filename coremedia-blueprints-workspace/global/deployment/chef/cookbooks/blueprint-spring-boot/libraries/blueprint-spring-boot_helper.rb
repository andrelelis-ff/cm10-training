def spring_boot_default(app_key, attribute_key, base_service = nil)
  global_default = node.deep_fetch('blueprint', 'spring-boot', attribute_key)
  service_default = node.deep_fetch('blueprint', 'spring-boot', app_key, attribute_key)
  base_service_default = base_service.nil? ? nil : node.deep_fetch('blueprint', 'spring-boot', base_service, attribute_key)
  result = global_default
  result = base_service_default unless base_service_default.nil?
  result = service_default unless service_default.nil?
  result
end
