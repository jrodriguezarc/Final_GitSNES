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

#_______________________________________________________________________________________________________________________________________________

def decryptMD5(n):    
    """ Validates the token    
        
        Parameters:
        :param n: token to validate
    """
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

    """ Gets all users in the DB

        Method: GET
        Path: /users/{hashing}
        
        Request Parameters:
        pretty          [true|false]        
        
        Parameters:
        :param hashing: the key identifies the token for having access to the API
    """

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

#_______________________________________________________________________________________________________________________________________________

#create user
class CreateUser(webapp2.RequestHandler):

    """ Creates a user if it does not exist

        Method: POST
        Path: /createuser/{user_nameP}/{user_emailP}/{hashing}
        
        Request Parameters:
        pretty          [true|false]        
        
        Parameters:
        :param user_nameP: the key that identifies the users's name
        :param user_emailP: the key that identifies the users's email
        :param hashing: the key identifies the token for having access to the API
    """

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

#_______________________________________________________________________________________________________________________________________________

class GetUserToken(webapp2.RequestHandler):

    """ Gets the token for having access 

        Method: GET
        Path: /token/{user_emailP}
        
        Request Parameters:
        pretty          [true|false]        
        
        Parameters:
        :param user_emailP: the key that identifies the users's email
    """

    def get(self, user_emailP):
        users = UserDB.all()
        users.filter("email =", user_emailP)
        result=[]
        for user in users:
            result.append({'id': str(user.key())})
        self.response.write(json.encode(result))

#_______________________________________________________________________________________________________________________________________________

class GamesRecomendation(webapp2.RequestHandler):

    """ Recomends a games according to the category of the favorite games of a specific user

        Method: GET
        Path: /gamesrecomendation/{user_idP}
        
        Request Parameters:
        pretty          [true|false]        
        
        Parameters:
        :param user_idP: the key that identifies the user
    """
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

#_______________________________________________________________________________________________________________________________________________

class Recomendations(webapp2.RequestHandler):
    """ Gets the first ten games with the highest amount of likes for recommending to the users
        Method: GET
        Path: /moreliked/{hashing}
        
        Request Parameters:
        pretty          [true|false]        
        
        Parameters:
        :param hashing: the key identifies the token for having access to the API
    """   

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

#_______________________________________________________________________________________________________________________________________________

class LikeRecomend(webapp2.RequestHandler):

    """ Gets games with the highest amount of likes for recommending to the users
        Method: GET
        Path: /recomendedliker/{hashing}
        
        Request Parameters:
        pretty          [true|false]        
        
        Parameters:
        :param hashing: the key identifies the token for having access to the API
    """    
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

class GameFavorite(webapp2.RequestHandler):

    """ Adds a favorite game if it does not exist
        Method: POST
        Path: /gamesfavoriteadd/{game_idP}/{user_idP}
        
        Request Parameters:
        pretty          [true|false]        
        
        Parameters:
        :param game_idP: the key that identifies the game
        :param user_idP: the key identifies the user
    """

    def post(self, game_idP, user_idP):
        countfavorite = FavoriteDB.all()
        key=countfavorite.filter("game_id =", game_idP).filter("user_id =",user_idP) 
        if key.count() == 0 :
             favoritegame= FavoriteDB(game_id=game_idP,user_id=user_idP)
             favoritegame.put() 
             self.response.write("Ok")
        else:
             self.response.write("Exist")

#_______________________________________________________________________________________________________________________________________________

class GamesFavorite(webapp2.RequestHandler):

    """ Gets all favorite games of a specific game

        Method: GET
        Path: /gamesfavorite/{user_id}
        
        Request Parameters:
        pretty          [true|false]        
        
        Parameters:
        :param user_id: the key that identifies the user 
    """
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

    """ Gets all the games from the DB

        Method: GET
        Path: /games/{hashing}
        
        Request Parameters:
        pretty          [true|false]        
        
        Parameters:
        :param hashing: the key identifies the token for having access to the API
    """

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
    
    """ Searches a game by the key 

        Method: GET
        Path: /GamesSearch/{application_key}
        
        Request Parameters:
        pretty          [true|false]        
        
        Parameters:
        :param application_key: the key that identifies the game
    """

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

#_______________________________________________________________________________________________________________________________________________
class DeleteGame(webapp2.RequestHandler):
    
    """ Deletes a specific game

        Method: DELETE
        Path: /deletegame/{application_key}/{hashing}
        
        Request Parameters:
        pretty          [true|false]        
        
        Parameters:
        :param application_key: the key that identifies the game
        :param hashing: the key identifies the token for having access to the API
    """

    def delete(self, game_key,hashing):
        if(decryptMD5(hashing)):
            self.response.content_type = 'application/json' 
            game = GameDB.get(game_key)
            db.delete(game)


#_______________________________________________________________________________________________________________________________________________

class LikeGame(webapp2.RequestHandler):

    """ Gives a like to a specific game

        Method: PUT
        Path: /likegame/{application_key}/{hashing}

        Request Parameters:
        pretty          [true|false]          

        Parameters:
        :param application_key: the key that identifies the game
        :param hashing: the key identifies the token for having access to the API
    """

    def put(self, game_key,hashing):
        if(decryptMD5(hashing)):
            self.response.content_type = 'application/json' 
            game = GameDB.get(game_key)
            game.likes = str(int(game.likes) + 1)
            db.put(game)

#_______________________________________________________________________________________________________________________________________________

class ApproveGame(webapp2.RequestHandler):

    """ Changes the state of the game to approved

        Method: PUT
        Path: /changestate/{application_key}/{hashing}

        Request Parameters:
        pretty          [true|false]          

        Parameters:
        :param application_key: the key that identifies the game
        :param hashing: the key identifies the token for having access to the API
    """

    def put(self, game_key,hashing):
        if(decryptMD5(hashing)):
            self.response.content_type = 'application/json' 
            game = GameDB.get(game_key)
            game.state = str(1)
            db.put(game)
#_______________________________________________________________________________________________________________________________________________

class Game(webapp2.RequestHandler):

    """ Uploads a game metadata

        Method: POST
        Path: /game/{uploader}/{game_name}/{game_description}/{game_category}/{_image}/{_file}/{hashing}
 
        Parameters:
        :param uploader: the key that identifies the game
        :param game_name: the name that identifies the game
        :param game_category: the category that identifies the game
        :param game_description: the description that identifies the game
        :param _image: the bucket-imageKey identifies the game image
        :param _file: the  bucket-fileKey that identifies the game file
        :param hashing: the key identifies the token for having access to the API
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
