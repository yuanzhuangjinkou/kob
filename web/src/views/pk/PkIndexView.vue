<template>
    <div v-if="$store.state.pk.status === 'playing' && $store.state.snackId == $store.state.user.id" class="div" v-bind:style="'background: '+ '#4876EC'" >  <!---->
        蛇的起始位置:左下角</div>
    <div v-if="$store.state.pk.status === 'playing' && $store.state.snackId != $store.state.user.id" class="div" v-bind:style="'background: '+ '#F94848'" >  <!---->
        蛇的起始位置:右上角</div>
    <PlayGround v-if="$store.state.pk.status === 'playing'"></PlayGround>
    <MatchGround v-if="$store.state.pk.status === 'matching'" />
    <ResultBoard v-if="$store.state.pk.loser !== 'none'" />
</template>

<script>
import PlayGround from '../../components/PlayGround.vue'
import MatchGround from '../../components/MatchGround.vue'
import ResultBoard from '../../components/ResultBoard.vue'
import {onMounted, onUnmounted} from 'vue'
import { useStore } from 'vuex'

export default {
    components: {
        PlayGround,
        MatchGround,
        ResultBoard,
    },
    setup() {
        
        let sId = -1;
        let co = "#4876EC";
        let data_id = -1;
        
        const store = useStore();
        const socketUrl = `ws://127.0.0.1:3000/websocket/${store.state.user.token}/`;
        
        store.commit("updateLoser", "none");
        store.commit("updateIsRecord", false);

        let socket = null;
        onMounted(() => {
            store.commit("updateOpponent", {
                username: "我的对手",
                photo: "https://cdn.acwing.com/media/article/image/2022/08/09/1_1db2488f17-anonymous.png",
            })
            socket = new WebSocket(socketUrl);

            socket.onopen = () => {
                console.log("connected!");
                store.commit("updateSocket", socket);
            }

            socket.onmessage = msg => {
                const data = JSON.parse(msg.data);
                if (data.event === "start-matching") {  // 匹配成功
                    // 左下角id
                    store.state.snackId = parseInt(data.game.a_id);
                    store.commit("updateOpponent", {
                        username: data.opponent_username,
                        photo: data.opponent_photo,
                    });
                    setTimeout(() => {
                        store.commit("updateStatus", "playing");
                    }, 200);
                    store.commit("updateGame", data.game);
                } else if (data.event === "move") {
                    console.log(data);
                    const game = store.state.pk.gameObject;
                    const [snake0, snake1] = game.snakes;
                    snake0.set_direction(data.a_direction);
                    snake1.set_direction(data.b_direction);
                } else if (data.event === "result") {
                    console.log(data);
                    const game = store.state.pk.gameObject;
                    const [snake0, snake1] = game.snakes;

                    if (data.loser === "all" || data.loser === "A") {
                        snake0.status = "die";
                    }
                    if (data.loser === "all" || data.loser === "B") {
                        snake1.status = "die";
                    }
                    store.commit("updateLoser", data.loser);
                }
            }

            socket.onclose = () => {
                console.log("disconnected!");
            }
        });

        onUnmounted(() => {
            socket.close();
            store.commit("updateStatus", "matching");
        })
    
        return {
            sId,
            co,
            data_id
        }
        
    }
}
</script>

<style scoped>
.div {
    color: #000000;
    text-align: center;
    border: 2px solid black;
    font-weight: bold;
    
}
</style>
