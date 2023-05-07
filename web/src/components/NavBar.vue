<template>
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container">
            <router-link class="navbar-brand" :to="{name: 'home'}">King Of Bots</router-link>
            <div class="collapse navbar-collapse" id="navbarText">
                <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                    <li class="nav-item">
                        <router-link :class="route_name == 'pk_index' ? 'nav-link active' : 'nav-link'"
                                     :to="{name: 'pk_index'}">对战
                        </router-link>
                    </li>
                    <li class="nav-item">
                        <router-link :class="route_name == 'record_index' ? 'nav-link active' : 'nav-link'"
                                     :to="{name: 'record_index'}">对局列表
                        </router-link>
                    </li>
                    <li class="nav-item">
                        <router-link :class="route_name == 'ranklist_index' ? 'nav-link active' : 'nav-link'"
                                     :to="{name: 'ranklist_index'}">排行榜
                        </router-link>
                    </li>
                    
                    <!-- Button trigger modal -->
                    <button type="button" class="btn btn-outline-primary" data-bs-toggle="modal" data-bs-target="#exampleModal">
                        游戏规则
                    </button>
                    <!-- Modal -->
                    <div class="modal fade" id="exampleModal" tabindex="-1" aria-labelledby="exampleModalLabel"
                         aria-hidden="true">
                        <div class="modal-dialog">
                            <div class="modal-content">
                                <div class="modal-header">
                                    <h5 class="modal-title" id="exampleModalLabel">Modal title</h5>
                                    <button type="button" class="btn-close" data-bs-dismiss="modal"
                                            aria-label="Close"></button>
                                </div>
                                <div class="modal-body">
                                  回合制操作
                                  亲自操作：玩家每回合需要在5秒中之内利用WASD控制蛇的下一回合移动方向
                                  AI操作：玩家需要在开始匹配前选择自己所使用的Bot
                                  胜负：
                                  玩家操作蛇尽可能的避开障碍物与对方蛇
                                  玩家下一回合无法移动或者违规操作则判断为失败
                                </div>
                                <div class="modal-footer">
                                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close
                                    </button>
<!--                                    <button type="button" class="btn btn-primary">Save changes</button>-->
                                </div>
                            </div>
                        </div>
                    </div>
                
                </ul>
                
                
                <ul class="navbar-nav" v-if="$store.state.user.is_login">
                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" id="navbarDropdown" role="button"
                           data-bs-toggle="dropdown" aria-expanded="false">
                            {{ $store.state.user.username }}
                        </a>
                        <ul class="dropdown-menu" aria-labelledby="navbarDropdown">
                            <li>
                                
                                <router-link class="dropdown-item" :to="{name: 'user_bot_index'}">我的Bot</router-link>
                            </li>
                            <li>
                                <hr class="dropdown-divider">
                            </li>
                            <li><a class="dropdown-item" href="#" @click="logout">退出</a></li>
                        </ul>
                    </li>
                </ul>
                <ul class="navbar-nav" v-else-if="!$store.state.user.pulling_info">
                    <li class="nav-item">
                        <router-link class="nav-link" :to="{name: 'user_account_login' }" role="button">
                            登录
                        </router-link>
                    </li>
                    <li class="nav-item">
                        <router-link class="nav-link" :to="{name: 'user_account_register'}" role="button">
                            注册
                        </router-link>
                    </li>
                </ul>
            </div>
        </div>
    </nav>
</template>

<script>
import {useRoute} from 'vue-router'
import {computed} from 'vue'
import {useStore} from 'vuex';

export default {
    setup() {
        const store = useStore();
        const route = useRoute();
        let route_name = computed(() => route.name)
        
        const logout = () => {
            store.dispatch("logout");
        }
        
        return {
            route_name,
            logout
        }
    }
}
</script>

<style scoped>

</style>
