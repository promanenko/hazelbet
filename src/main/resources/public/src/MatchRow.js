export const MatchRow = {
    props: ['data'],
    computed: {

    },
    methods: {
        setFirstTeamBet() {
            this.showBetWindow({
                outcome: 'WIN_1',
                matchId: this.data.id,
                team: this.data.firstTeam,
                coefficient: this.data.winFirst
            })
        },
        setDrawBet() {
            this.showBetWindow({
                outcome: 'DRAW',
                coefficient: this.data.draw
            })
        },
        setSecondTeamBet() {
            this.showBetWindow({
                outcome: 'WIN_2',
                matchId: this.data.id,
                team: this.data.secondTeam,
                coefficient: this.data.winSecond
            })
        },
        ...Vuex.mapActions(['showBetWindow'])
    },
    template: `
        <tr>
          <td>{{ data.firstTeam }} - {{ data.secondTeam }}</td>
          <td><b>{{ data.firstScored  }} : {{ data.secondScored }}</b></td>
          <td class="clickable" @click="setFirstTeamBet"> {{ data.winFirst }} <indicator :state="data.winFirstTrend" /></td>
          <td class="clickable" @click="setDrawBet">{{ data.draw }} <indicator :state="data.drawTrend" /></td>
          <td class="clickable" @click="setSecondTeamBet">{{ data.winSecond }} <indicator :state="data.winSecondTrend" /></td>
        </tr>
    `
}