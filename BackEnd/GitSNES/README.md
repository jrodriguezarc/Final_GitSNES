#API Reference

The following resources are made available by the *GitSNes* API:

* [Games](#games)

##Games

Properties of the entity

| Property    | Data Type | Description                                                                             |
|:------------|-----------|:----------------------------------------------------------------------------------------|
| id          | string    | Key in the datastore										        					|
| name        | string    | Name of the Game                 														|
| uploader	  | string    | Key of the user who uploads a games 													|
| description | string    | Brief description of the caracteristics  of the game                                    |
| category    | string    | Category of the name                                                                    |
| state    	  | string    | State for identifying if a game is pending                                              |
| likes       | string    | Amount of likes                                                                         |
| img_url     | string    | Url where the image is stored                                                           |
| file_url    | string    | Url where the game is stored                                                            |
| uploaddate  | string    | Date in which the game was added                            							|


###Methods

| Method | Request URI                              											| Description                                                                  |
|:-------|:-------------------------------------------------------------------------------------|:-----------------------------------------------------------------------------|
| GET    | [/games/*{hashing}*]                       											| Gets all the games from the DB                                               |
| GET    | [/users/*{hashing}*]                          										| Gets all users in the DB                                                     |
| GET    | [/token/*{user_emailP}*]                                                             | Gets the token for having access                                             |
| GET    | [/gamesrecomendation/*{user_idP}*]                                                   | Recomends games according to the category of the favorite games of a users   |
| GET    | [/moreliked/*{hashing}*]                                                             | Recomends games according to the first games with the highest amount of likes|
| GET    | [/recomendedliker/*{hashing}*]                                                       | Gets games with the highest amount of likes for recommending to the users    |
| GET    | [/gamesfavorite/*{user_id}*]                                                         | Gets all favorite games of a specific game                                   |
| GET    | [/GamesSearch/*{application_key}*]													| Searches a game by the key                                                   |
| POST   | [/gamesfavoriteadd/*{game_idP}*/*{user_idP}*]                                        | Adds a favorite game if it does not exist                                    |
| POST   | [/createuser/*{user_nameP}*/*{user_emailP}*/*{hashing}*]                             | Creates a user if it does not exist                                          |
| POST   | [/game/{uploader}/*{game_name}*/*{game_description}*/*{game_category}*/*{_image}*/*{_file}*/*{hashing}*] | Add a game in the system                                                     |
| PUT    | [/likegame/*{application_key}*/*{hashing}*]		                                    | Gives a like to a specific game                                              |
| PUT    | [/changestate/*{application_key}*/*{hashing}*]		                                | Changes the state of the game to approvedgame                                |
| DELETE | [/deletegame/*{application_key}*/*{hashing}*]		                                | Deletes a specific game                                                      |

