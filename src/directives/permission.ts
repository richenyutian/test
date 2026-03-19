import type { App, DirectiveBinding } from 'vue'

import pinia from '../stores'
import { useAuthStore } from '../stores/auth'

function checkPermission(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
  const authStore = useAuthStore(pinia)
  const permissions = Array.isArray(binding.value) ? binding.value : [binding.value]
  const matched = permissions.some((permission) => authStore.hasButtonPermission(permission))

  if (!matched) {
    el.parentNode?.removeChild(el)
  }
}

export default {
  install(app: App) {
    app.directive('permission', {
      mounted(el, binding) {
        checkPermission(el, binding)
      },
      updated(el, binding) {
        checkPermission(el, binding)
      },
    })
  },
}
