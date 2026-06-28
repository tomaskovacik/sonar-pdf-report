require 'base64'
require 'fileutils'

class PdfReportController < ApplicationController
  protect_from_forgery with: :exception
  # store is called server-to-server with a Bearer token, not cookie-based, so CSRF doesn't apply
  skip_before_action :verify_authenticity_token, only: [:store]

  def get
    project = Project.by_key(params[:resource])
    send_file Rails.root.join('pdf-files', project.key.gsub(':', '-') + '.pdf'), :type => 'application/pdf', :disposition => 'attachment'
  end

  def store
    uploaded = params[:upload]
    filename = params[:pdfname] || uploaded.original_filename
    # Rails.root is WEB-INF dir
    FileUtils::mkdir_p Rails.root.join('pdf-files') unless File.exists?(Rails.root.join('pdf-files'))
    File.open(Rails.root.join('pdf-files', filename), 'wb') do |file|
      file.write(uploaded.read)
    end
    render :nothing => true, :status => 200
  end
end