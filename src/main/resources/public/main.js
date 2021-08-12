import { Matches } from './src/Matches.js'
import { Spinner } from './src/Spinner.js'
import { store } from './src/store.js'

const Counter = {
    template: `
        <matches />
    `
}

const app = Vue.createApp(Counter)
    .component('matches', Matches)
    .component('spinner', Spinner)

app.use(store)
app.mount('#app')