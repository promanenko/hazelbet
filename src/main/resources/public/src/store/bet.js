export const bet = {
    state:() => ({
        data: null,
    }),
    mutations: {
        setBet(state, data) {
            state.data = data;
        },
    },
    actions: {
        showBetWindow({ commit }, data) {
            commit('setBet', data)
        },
        hideBetWindow({ commit }) {
            commit('setBet', null)
        }
    }
}