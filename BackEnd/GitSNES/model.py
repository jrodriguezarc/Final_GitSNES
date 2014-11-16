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

from google.appengine.ext import db

class GameDB(db.Model):
    name = db.StringProperty(required=True)
    uploader = db.StringProperty(required=True)
    description = db.StringProperty(required=True)
    category = db.StringProperty(required=True)
    state = db.StringProperty()
    likes = db.StringProperty()
    image_url = db.StringProperty()
    file_url= db.StringProperty()
    uploaddate = db.DateProperty()

class UserDB(db.Model):
    name = db.StringProperty(required=True)
    email = db.StringProperty(required=True)

class AdministratorDB(db.Model):
    name = db.StringProperty(required=True)
    email = db.StringProperty(required=True)
    password = db.StringProperty(required=True)    

class FavoriteDB(db.Model):
    game_id = db.StringProperty(required=True)
    user_id = db.StringProperty(required=True)

class LikeDB(db.Model):
    game_id = db.StringProperty(required=True)
    user_id = db.StringProperty(required=True)

class PendingGameDB(db.Model):
    game_id = db.StringProperty(required=True)

class hashDB(db.Model):
    code = db.StringProperty(required=True)
    uploaddate = db.DateTimeProperty()