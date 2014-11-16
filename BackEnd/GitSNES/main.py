# !/usr/bin/env python

# Copyright 2014 GitSNES
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
import webapp2


from game import Games, Game, GamesSearch, DeleteGame, LikeGame, ApproveGame, Recomendations, Users, CreateUser, GamesRecomendation, GameFavorite, GamesFavorite, GetUserToken, LikeRecomend
from administrator import GameAdministrator


app = webapp2.WSGIApplication([
					(r'/users/(\w+)', Users),						#HS
					(r'/createuser/(\w+)/(.*)/(\w+)', CreateUser),	#HS
					(r'/token/(.*)', GetUserToken),
					
				    
				    (r'/gamesrecomendation/([a-zA-Z0-9_-]+)', GamesRecomendation),
				    (r'/gamesfavorite/([a-zA-Z0-9_-]+)', GamesFavorite), 
				    (r'/gamesfavoriteadd/([a-zA-Z0-9_-]+)/([a-zA-Z0-9_-]+)', GameFavorite), 
				    
				    (r'/games/(\w+)', Games),									#HS
				    (r'/moreliked/(\w+)', Recomendations),						#HS
				    (r'/recomendedliker/(\w+)', LikeRecomend),					#HS
				    (r'/game/(\w+)/(\w+)/(\w+)/(\w+)/(\w+)/(\w+)/(\w+)', Game), #HS
				    
				    (r'/GamesSearch/([a-zA-Z0-9_-]+)', GamesSearch),
				    
				   	
				   	(r'/deletegame/([a-zA-Z0-9_-]+)/(\w+)', DeleteGame), 	 #HS
				   	
				   	(r'/likegame/([a-zA-Z0-9_-]+)/(\w+)', LikeGame),		 #HS			  
				   	(r'/changestate/([a-zA-Z0-9_-]+)/(\w+)', ApproveGame),   #HS

				    (r'/administrator/(\w+)/(\w+)/(\w+)', GameAdministrator)], debug=True)