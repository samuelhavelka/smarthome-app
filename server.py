import os
from flask import Flask, flash, request
from werkzeug.utils import secure_filename


UPLOAD_FOLDER = os.path.join(os.path.dirname(__file__), 'videos')
ALLOWED_EXTENSIONS = {'mp4'}

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER


def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS


@app.route('/')
def showHomePage():
      # response from the server
    return "This is home page"


@app.route('/upload', methods=['GET', 'POST'])
def upload_file():
    if request.method == 'POST':

        # check if the post request has the file part
        if 'file' not in request.files:
            flash('No file part')
            return "no file part"
        
        file = request.files.get('file')

        if file.filename == '':
            flash('No selected file')
            return "No file selected"
        
        i = 1
        filename_server = file.filename.replace(".mp4", "") + "_" + str(i) + "_HAVELKA.mp4"
        while os.path.exists(os.path.join(app.config['UPLOAD_FOLDER'], filename_server)):
            filename_server = file.filename.replace(".mp4", "") + "_" + str(i) + "_HAVELKA.mp4"
            i += 1
        
        if file and allowed_file(file.filename):
            filename = secure_filename(filename_server)
            file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
            return "File Uploaded Successfully"
        
    return "upload_return"


if __name__ == '__main__':
   app.run(host='0.0.0.0', port=5000, debug=True)