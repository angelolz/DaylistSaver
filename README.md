# DaylistSaver

This app automatically looks for a new update to your [daylist](https://newsroom.spotify.com/2023-09-12/ever-changing-playlist-daylist-music-for-all-day/), and create a new playlist for them so you never lose it.

The daylist creates a new playlist with a different playlist ID on update, which means that unless you have the link of the playlist before it is updated, there's no way for you to get the old (as far as I know).

This project is still a WIP, as I have to put a couple more features to feel complete.

# Dependencies
- [spotify-web-api-java](https://github.com/spotify-web-api-java/spotify-web-api-java)
- [jetty](https://github.com/jetty/jetty.project)
- [gson](https://github.com/google/gson)
- [pug4j](https://github.com/neuland/pug4j)
- [logback](https://github.com/qos-ch/logback)
- [slf4j](https://github.com/qos-ch/slf4j)

# Building / Running
1) Clone this repo. Create a new file called `config.properties`.
   - Run executables will be created soon.
2) Make a new app [here](https://developer.spotify.com/dashboard). Fill in the app name and description to whatever you want and check the "Web API" checkbox.
   - Set the Redirect URL to "http://localhost:4050/callback"
3) Inside `config.properties`, put these fields in:
   ```properties
   spotify_client_id=
   spotify_client_secret=
   ```
4) Copy the client ID, client secret and redirect uri to their respective fields.
5) Start the app. Upon startup, the app will prompt you to visit `http://localhost:4050/login` to authenticate with your Spotify account.
6) After authenticating, the app will start checking for daylist updates every hour.

### Extra configuration settings
There are extra settings you can set described below, if not set then they will be set to default values:
- `port` - sets the port where the webserver will run on. This port will be used on the `redirect_url`'s default value as well. Must be a **number**.
  - default: `4050`
- `save_playlists` - A `true`/`false` value that toggles whether to create a new Spotify playlist automatically when your daylists updates.
  - default: `false`
- `ask_for_code` - This setting toggles whether it asks for your authorization code on startup. This does not disable the `/login` endpoint.
  - Useful for when you run this app on a server and don't want to use the webserver to login.
  - default: `false`
- `redirect_url` - Changes the redirect url after authenticating with your spotify. **Used when `ask_for_code` is `true`**.
  - When changing this, make sure you put the redirect url in your app settings on your developer dashboard, or else it will not work.
  - default: `http://localhost:{port}/callback`
- `timezone` - determines what timezone to use when getting the current date to use for new playlist names.
  - Valid timezones can be found [here](https://garygregory.wordpress.com/2013/06/18/what-are-the-java-timezone-ids/).
  - default: `null`, app will determine the timezone from current time on the machine it's running on.

# License
MIT