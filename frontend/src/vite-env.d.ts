declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

declare module 'frappe-gantt' {
  export default class Gantt {
    constructor(wrapper: string | HTMLElement, tasks: any[], options?: any)
    change_view_mode(mode: string): void
  }
}
