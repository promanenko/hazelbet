import { formatAmount } from './formatters.js';
import { api } from './api.js'


export const BetOverlay = {
    data() {
      return {
          betId: null,
          processing: false,
          betAmount: '10'
      }
    },
    computed: {
        data() {
            return this.$store.state.bet.data
        },
        coefficient() {
            switch(this.data.outcome) {
                case 'WIN_1':
                    return this.match.winFirst
                case 'WIN_2':
                    return this.match.winSecond
                default:
                    return this.match.draw
            }
        },
        title() {
            switch(this.data.outcome) {
                case 'WIN_1':
                    return `${this.match.firstTeam} W`
                case 'WIN_2':
                    return `${this.match.secondTeam} W`
                default:
                    return 'Draw'
            }
        },
        caption() {
            switch(this.data.outcome) {
                case 'DRAW':
                    return ''
                default:
                    return `${this.match.firstTeam} - ${this.match.secondTeam}`
            }
        },
        possibleWin() {
            return formatAmount((this.betAmount * 100 * this.coefficient * 100) / 10000)
        },
        match() {
            return this.$store.getters.getMatchById(this.data.matchId)
        }
    },
    methods: {
        onClose() {
            clearTimeout(this.timer)
            this.betAmount = '10'
            this.betId = null
            this.processing = false
            this.hideBetWindow()
        },
        makeBet() {
            (async() => {
                try {
                    this.processing = true
                    const { betId } = await api('/bets', {
                        method: 'POST',
                        body: JSON.stringify({
                            matchId: this.data.matchId,
                            outcome: this.data.outcome,
                            amount: this.betAmount,
                            coefficient: this.coefficient
                        })
                    })

                    if (betId) {
                        const getStatus = async () => {
                            try {
                                const res = await api(`/bets/${betId}`)
                                if (res) {
                                    this.onClose()
                                }
                            } catch (e) {
                                this.timer = setTimeout(getStatus, 1000)
                            }
                        }

                        getStatus()
                    }
                } catch(e) {
                    console.log(e)
                }
            })()
        },
        ...Vuex.mapActions(['hideBetWindow']),
    },
    unmounted() {
        clearTimeout(this.timer)
    },
    template: `
        <overlay v-if="data" :onClose="onClose">
            <template v-slot:content>
                <div v-if="!processing">
                    <h2>
                        {{ title }} <span class="float-end">{{  coefficient }}</span>
                    </h2>
                    <span class="caption">{{ caption }}</span>
                    <div class="mb-3">
                        <label for="amount" class="form-label">Amount</label>
                        <input class="form-control" id="amount" v-maska="{ mask: '#*.##'}" v-model="betAmount" />
                    </div>
                    <span>Possible win: {{ possibleWin }}</span>
                </div>
                <spinner v-if="processing" />
            </template>
            <template v-slot:footer>
                <button v-if="!processing" class="btn btn-primary me-2" @click="makeBet" :disabled="processing">
                    Make a bet
                </button>
            </template>
        </overlay>
    `
}