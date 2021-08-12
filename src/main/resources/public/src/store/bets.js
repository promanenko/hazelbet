import { api } from '../api.js'
/*
{
    amount: 10
    coefficient: 2.1
    id: "2845579b-acca-4a5b-98f3-836ce3526184"
    matchId: 1
    outcome: "WIN_1"
    reason: "Match is suspended"
    rejected: true
    success: false
    userId: 1
}*/

export const bets = {
    state:() => ({
        data: [],
        loading: false,
    }),
    mutations: {
        setBets(state, data) {
            state.data = data
        },
        setLoading(state, data) {
            state.loading = data
        }
    },
    actions: {
        loadBets: async({ commit, dispatch }) => {
            try {
                dispatch('setLoading', true)
                const data = await api('/bets')

                commit('setBets', data)

                dispatch('setLoading', false)
            } catch(e) {
                console.log(e)
            }
        }
    }
}