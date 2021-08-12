import { Matches } from './src/Matches.js'
import { MatchRow } from './src/MatchRow.js'
import { Spinner } from './src/Spinner.js'
import { Header } from './src/Header.js'
import { Indicator } from './src/Indicator.js'
import { Overlay } from './src/Overlay.js'
import { BetOverlay } from './src/BetOverlay.js'
import { store } from './src/store.js'
import { router } from './src/route.js';

const Counter = {
    template: `
        <app-header />
        <div class="mb-3">
            <router-link to="/" class="me-3">Matches</router-link>
            <router-link to="/bets">Bets</router-link>
        </div>
        <router-view></router-view>
        <bet-overlay />
    `
}

const app = Vue.createApp(Counter)
    .component('matches', Matches)
    .component('spinner', Spinner)
    .component('app-header', Header)
    .component('indicator', Indicator)
    .component('overlay', Overlay)
    .component('match-row', MatchRow)
    .component('bet-overlay', BetOverlay)

app.use(store)
app.use(router)
app.directive('maska', Maska.maska);
app.mount('#app')