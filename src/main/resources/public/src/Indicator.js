export const Indicator = {
    props: ['state'],
    computed: {
        icon() {
            switch(this.state) {
                case 'UP':
                    return 'bi bi-arrow-up success'
                case 'DOWN':
                    return 'bi bi-arrow-down warning'
                default:
                    return ''
            }
        }
    },
    template: `
        <i :class="icon"></i>
    `
}