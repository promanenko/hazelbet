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
        possibleWin() {
            return formatAmount((this.betAmount * 100 * this.data.coefficient * 100) / 10000)
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
                            coefficient: this.data.coefficient
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
                        {{ data.team ? data.team + " W" :  'Draw' }} {{  data.coef }}
                    </h2>
                    <div class="mb-3">
                        <label for="amount" class="form-label">Amount</label>
                        <input class="form-control" id="amount" v-maska="{ mask: '#*.##'}" v-model="betAmount" />
                    </div>
                    <span>Possible win: {{ possibleWin }}</span>
                </div>
                <spinner v-if="processing" />
            </template>
            <template v-slot:footer>
                <button v-if="!processing" class="btn btn-primary me-2" @click="makeBet" :disabled="processing">Make a bet</button>
            </template>
        </overlay>
    `
}