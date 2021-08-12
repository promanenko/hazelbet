import { Matches } from './Matches.js'
import { Bets } from './Bets.js'

const routes = [
    { path: '/', component: Matches },
    { path: '/bets', component: Bets },
]

export const router = VueRouter.createRouter({
    history: VueRouter.createWebHashHistory(),
    routes,
})