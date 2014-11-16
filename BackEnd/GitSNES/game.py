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
import datetime
from model import GameDB, UserDB, AdministratorDB, FavoriteDB, LikeDB, PendingGameDB, hashDB
from webapp2_extras import json
from google.net.proto.ProtocolBuffer import ProtocolBufferDecodeError
from google.appengine.api.datastore_errors import TransactionFailedError, BadValueError
from google.appengine.ext import db
from google.appengine.api import users
import hashlib
import sys

def decryptMD5(n):    
    for x in range(1,500):
        if( hashlib.md5(hashlib.md5(str((x*(x+1)*(4*x-1)/6))).hexdigest()).hexdigest() == n):
            query = hashDB.all()            
            query.order("-uploaddate")
            for hashes in query.run(limit=3):
                if(hashes.code == n):
                    return False
            coding = hashDB(code=n)
            coding.uploaddate = datetime.datetime.now()
            coding.put()
            return True
    return False


"""class Hashes1(webapp2.RequestHandler):
    def get(self):
        self.response.content_type = 'application/json' 
        games = hashDB.all()
        games.order("-uploaddate")
        games.fetch(4)
        result=[]
        for game in games.run(limit=3):
            result.append({'': str(game.uploaddate), 
                            'code': game.code,
                             })
        self.response.write(json.encode(result))"""

#_______________________________________________________________________________________________________________________________________________
class Users(webapp2.RequestHandler):
    def get(self, hashing):
        if(decryptMD5(hashing)):
            self.response.content_type = 'application/json' 
            users = UserDB.all()
            result=[]
            for user in users:
                result.append({'id': str(user.key()), 
                                'name': user.name,
                                'email': user.email,})
            self.response.write(json.encode(result))

#create user
class CreateUser(webapp2.RequestHandler):
    def post(self, user_nameP, user_emailP , hashing):
        if(decryptMD5(hashing)):
            users = UserDB.all()
            count_users = users.filter("email =", user_emailP)
            if count_users.count() == 0 :
                user= UserDB(name=user_nameP,email=user_emailP)
                user.put()
                self.response.write('OK' )
            else:
                 self.response.write("Exist")

class GetUserToken(webapp2.RequestHandler):
    def get(self, user_emailP):
        users = UserDB.all()
        users.filter("email =", user_emailP)
        result=[]
        for user in users:
            result.append({'id': str(user.key())})
        self.response.write(json.encode(result))


#_______________________________________________________________________________________________________________________________________________
class GamesRecomendation(webapp2.RequestHandler):
    def get(self, user_idP):
        self.response.content_type = 'application/json' 
        
        usergames = FavoriteDB.all()
        usergames.filter("user_id =",user_idP) 

        result_usergames =[]
        for usergame in usergames:
            result_usergames.append(usergame.game_id,)

        result_categorygames = []
        categorygames = GameDB.all()
        for categorygame in categorygames:
            if str(categorygame.key()) in result_usergames:
                result_categorygames.append(categorygame.category)

        finalresult = []
        games = GameDB.all()
        for game in games:
            if game.category in result_categorygames:
                finalresult.append({'id': str(game.key()), 
                                'name': game.name,
                                'uploader': game.uploader,
                                'description': game.description,
                                'category': game.category,
                                'file_url': game.file_url,
                                'image_url': game.image_url,
                                'uploaddate': str(game.uploaddate)},)
        self.response.write(json.encode(finalresult))


class Recomendations(webapp2.RequestHandler):
    def get(self,hashing):
        if(decryptMD5(hashing)):
            self.response.content_type = 'application/json' 
            games = GameDB.all()
            games.order("-likes")
            result=[]
            for game in games.run(limit=10):
                result.append({'id': str(game.key()), 
                                'name': game.name,
                                'uploader': game.uploader,
                                'description': game.description,
                                'category': game.category,
                                'file_url': game.file_url,
                                'image_url': game.image_url,
                                'uploaddate': str(game.uploaddate),
                                'state': game.state, 
                                'likes': game.likes,})
            self.response.write(json.encode(result))


class LikeRecomend(webapp2.RequestHandler):
    def get(self,hashing):
        if(decryptMD5(hashing)):
            self.response.content_type = 'application/json' 
            games = GameDB.all()
            games.order("-likes");        
            result=[]
            for game in games:
                if(game.likes != "0"):
                    result.append({'id': str(game.key()), 
                                    'name': game.name,
                                    'uploader': game.uploader,
                                    'description': game.description,
                                    'category': game.category,
                                    'file_url': game.file_url,
                                    'image_url': game.image_url,
                                    'uploaddate': str(game.uploaddate),
                                    'state': game.state, 
                                    'likes': game.likes,})
            self.response.write(json.encode(result))


#_______________________________________________________________________________________________________________________________________________
#Add favorite game if it does not exist
class GameFavorite(webapp2.RequestHandler):
    def post(self, game_idP, user_idP):
        countfavorite = FavoriteDB.all()
        key=countfavorite.filter("game_id =", game_idP).filter("user_id =",user_idP) 
        if key.count() == 0 :
             favoritegame= FavoriteDB(game_id=game_idP,user_id=user_idP)
             favoritegame.put() 
             self.response.write("Ok")
        else:
             self.response.write("Exist")


#Get all favorite games of a specific game
class GamesFavorite(webapp2.RequestHandler):
    def get(self, user_idP):
        self.response.content_type = 'application/json' 
        favoritegames = FavoriteDB.all()
        favoritegames.filter("user_id =",user_idP) 

        result=[]
        finalresult=[]

        for favoritegame in favoritegames:
            result.append(favoritegame.game_id,)

        games = GameDB.all()
        for game in games:
            if str(game.key()) in result:
                finalresult.append({'id': str(game.key()), 
                                'name': game.name,
                                'uploader': game.uploader,
                                'description': game.description,
                                'category': game.category,
                                'file_url': game.file_url,
                                'image_url': game.image_url,
                                'uploaddate': str(game.uploaddate)},)
        self.response.write(json.encode(finalresult))


#_______________________________________________________________________________________________________________________________________________
class Games(webapp2.RequestHandler):

    def get(self,hashing):
        if(decryptMD5(hashing)):
            self.response.content_type = 'application/json' 
            games = GameDB.all()
            games.order("-uploaddate")
            result=[]
            for game in games:
                result.append({'id': str(game.key()), 
                                'name': game.name,
                                'uploader': game.uploader,
                                'description': game.description,
                                'category': game.category,
                                'file_url': game.file_url,
                                'image_url': game.image_url,
                                'uploaddate': str(game.uploaddate),
                                'state': game.state, 
                                'likes': game.likes, })
            self.response.write(json.encode(result))



#_______________________________________________________________________________________________________________________________________________
class GamesSearch(webapp2.RequestHandler):
    def get(self,game_key):
        self.response.content_type = 'application/json' 
        game = GameDB.get(game_key)
        obj= {'id': 'cambiar', 
                            'name': game.name,
                            'uploader': game.uploader,
                            'description': game.description,
                            'category': game.category,
                            'file_url': game.file_url,
                            'image_url': game.image_url,
                            'uploaddate': str(game.uploaddate),
                            'state': game.state,
                            'likes': game.likes,}
        self.response.write(json.encode(obj))


#  ---------- DELETE API  ----------

#_______________________________________________________________________________________________________________________________________________
class DeleteGame(webapp2.RequestHandler):
    
    """ Deletes a game

        Method: DELETE
        Path: /deletegame/{application_key}

        URI Parameters:
        application_key string              the key that identifies the game
        
        Request Parameters:
        pretty          [true|false]        
        
        Parameters:
        :param application_key: the key that identifies the game
    """

    def delete(self, game_key,hashing):
        if(decryptMD5(hashing)):
            self.response.content_type = 'application/json' 
            game = GameDB.get(game_key)
            db.delete(game)


#  ----------  PUT API  ----------




class LikeGame(webapp2.RequestHandler):

    """ Like a game

        Method: PUT
        Path: /likegame/{application_key}

        Request Parameters:
        pretty          [true|false]          

        Parameters:
        :param application_key: the key that identifies the game
    """

    def put(self, game_key,hashing):
        if(decryptMD5(hashing)):
            self.response.content_type = 'application/json' 
            game = GameDB.get(game_key)
            game.likes = str(int(game.likes) + 1)
            db.put(game)


class ApproveGame(webapp2.RequestHandler):

    """ ApproveGame a game state

        Method: PUT
        Path: /changestate/{application_key}

        Request Parameters:
        pretty          [true|false]          

        Parameters:
        :param application_key: the key that identifies the game
    """

    def put(self, game_key,hashing):
        if(decryptMD5(hashing)):
            self.response.content_type = 'application/json' 
            game = GameDB.get(game_key)
            game.state = str(1)
            db.put(game)

#  ----------  POST API  ----------

#_______________________________________________________________________________________________________________________________________________

class Game(webapp2.RequestHandler):

    """ Upload a game metadata

        Method: POST
        Path: /deletegame/game/{uploader}/{name}/{category}/{description}/{imgURL}/{fileURL}/{initial_likes}

        Request Parameters:
        pretty          [true|false]    

        Parameters:
        :param uploader: the key that identifies the game
        :param name: the name that identifies the game
        :param category: the category that identifies the game
        :param description: the description that identifies the game
        :param imgURL: the bucket-imageKey identifies the game image
        :param fileURL: the  bucket-fileKey that identifies the game file
        :param initial_likes: the key that identifies the initial likes of the game
    """

    def post(self, uploader, game_name, game_description, game_category,_image,_file, hashing):
        if(decryptMD5(hashing)):
            game=GameDB(name=game_name,
                        uploader=uploader,
                        description=game_description,
                        file_url=_file,
                        image_url=_image,
                        category=game_category,
                        state=str(0),
                        likes=str(0))
            game.uploaddate = datetime.datetime.now().date()
            game.put()
            game_id = str(game.key().id())

            gamepending= PendingGameDB(game_id=game_id)
            gamepending.put()



#_______________________________________________________________________________________________________________________________________________
