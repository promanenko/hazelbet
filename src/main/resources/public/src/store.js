import { matches } from './store/matches.js'
import { user} from './store/user.js'
import { bet } from './store/bet.js'
import { bets } from './store/bets.js'

export const store = Vuex.createStore({
    modules: {
        bet,
        user,
        bets,
        matches
    }
})