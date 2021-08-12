import { formatAmount } from './formatters.js';

export const Bets = {
    methods: {
        formatAmount,
        ...Vuex.mapActions(['loadBets'])
    },
    computed: {
        items() {
            return this.$store.state.bets.data
        },
        loading() {
            return this.$store.state.bets.loading
        }
    },
    created() {
        this.loadBets()
    },
    template: `
        <spinner v-if="loading" />
        <table v-if="!loading" class="table table-striped">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Amount</th>
                    <th>Coefficient</th>
                    <th>Outcome</th>
                    <th>Match ID</th>
                    <th>User ID</th>
                    <th>Reason</th>
                </tr>
            </thead>
            <tbody>
                <tr v-if="items.length === 0"><td class="text-center" colspan="7">No data</td></tr>
                <tr v-for="(data, index) in items" :class="{'table-danger': data.rejected}">
                    <td>{{ data.id }}</td>
                    <td>{{ formatAmount(data.amount) }}</td>
                    <td>{{ data.coefficient }}</td>
                    <td>{{ data.outcome }}</td>
                    <td>{{ data.matchId }}</td>
                    <td>{{ data.userId }}</td>
                    <td>{{ data.reason }}</td>
                </tr>
            </tbody>
        </table>
    `
}