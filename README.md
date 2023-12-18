# DaylistSaver

This app automatically looks for a new update to your [daylist](https://newsroom.spotify.com/2023-09-12/ever-changing-playlist-daylist-music-for-all-day/), and create a new playlist for them so you never lose it.

The daylist creates a new playlist with a different playlist ID on update, which means that unless you have the link of the playlist before it is updated, there's no way for you to get the old (as far as I know).

This project is still a WIP, as I have to put a couple more features to feel complete, such as a better callback page where you can easily copy your authorization token, save the playlist link instead of making a whole new playlist instead, etc.

# Dependencies
- [spotify-web-api-java](https://github.com/spotify-web-api-java/spotify-web-api-java)

# Building / Running
1) Clone this repo. Create a new file called `config.properties`.
    - Run executables will be created soon.
2) Make a new app [here](https://developer.spotify.com/dashboard). Fill in the app name, description, redirect url, and check the "Web API" checkbox.
3) Inside `config.properties`, put these fields in:
   ```properties
   spotify_client_id=
   spotify_client_secret=
   redirect_uri=
   ```
4) Copy the client ID, client secret and redirect uri to their respective fields.
5) Start the app. Upon startup, the app will ask for an authentication code. Visit the link shown. Once you've approved the request, you will see your authorization code in this format below:
   ```
   https://your-redirect-url.com?code=<your-auth-code-here>
   ```
6) The app will check for a new daylist update every hour.